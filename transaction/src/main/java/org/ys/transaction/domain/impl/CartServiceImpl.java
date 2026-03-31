package org.ys.transaction.domain.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.domain.vo.CartItem;
import org.ys.transaction.domain.inteface.CartService;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.dao.YsOrderDao;
import org.ys.transaction.Infrastructure.dao.YsShoppingHistoryDao;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.domain.enums.OrderStatusEnum;
import org.ys.transaction.Infrastructure.pojo.YsGoods;
import org.ys.transaction.Infrastructure.pojo.YsOrder;
import org.ys.transaction.Infrastructure.pojo.YsShoppingHistory;
import org.ys.transaction.Infrastructure.pojo.YsUser;
import org.ys.transaction.Infrastructure.utils.IDUtils;
import org.ys.transaction.Infrastructure.utils.JsonUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    //秒杀商品key
    private static final String STOCK_PREFIX = "seckill:stock";
    //秒杀获得分布式锁前缀
    private static final String STOCK_LOCK = "seckill:lock:itemId:";
    //用户秒杀订单key
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";

    private static final String USER_RECORD_PREFIX = "seckill:user:record:";
    //用户购物车key
    private static final String SHOP_CAR_PREFIX = "shoppingCar:order:";
    //用户订单key
    private static final String NORMAL_USER_ORDER_PREFIX = "normal:user:order:";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CartServiceImpl.class);

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private YsOrderDao ysOrderDao;

    @Resource
    private YsGoodsDao goodsDao;

    @Resource
    private YsUserDao userDao;

    @Value("${seckill.orderTimeOut}")
    private long orderTimeOut;

    @Value("${normal.orderTimeOut}")
    private long normalOrderTimeOut;

    @Value("${shoppinCar.timeOut}")
    private long shoppinCarTimeOut;

    @Resource
    private YsShoppingHistoryDao shoppingHistoryDao;

    @Override
    public void addCart(CartItem cartItem) {
        String cartKey = SHOP_CAR_PREFIX + cartItem.getUserId();
        RMap<String, String> map = redissonClient.getMap(cartKey, new StringCodec());
        String itemId = String.valueOf(cartItem.getItemId());

        if (map.containsKey(itemId)) {
            String existingItemJson = map.get(itemId);
            CartItem existingItem = JsonUtils.jsonToPojo(existingItemJson, CartItem.class);
            existingItem.setNum(existingItem.getNum() + cartItem.getNum());
            map.put(itemId, JsonUtils.objectToJson(existingItem));
        } else {
            map.put(itemId, JsonUtils.objectToJson(cartItem));
        }
        map.expire(shoppinCarTimeOut, TimeUnit.DAYS);
    }

    @Override
    public List<CartItem> showCart(Long userId) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
        cartMap.expire(shoppinCarTimeOut, TimeUnit.DAYS);
        List<CartItem> items = new ArrayList<>();
        for (String json : cartMap.readAllValues()) {
            if (json == null || json.isEmpty()) continue;
            CartItem item = JsonUtils.jsonToPojo(json, CartItem.class);
            if (item != null) items.add(item);
        }
        return items;
    }

    @Override
    public void deleteById(Long itemId, Long userId) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
        cartMap.remove(String.valueOf(itemId));
    }

    @Override
    public void updateCartNum(Long itemId, Long userId, Integer num) {
        if (itemId == null || userId == null) throw new IllegalArgumentException("参数错误");
        if (num == null || num < 1) throw new IllegalArgumentException("数量必须大于等于1");

        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
        String itemJson = cartMap.get(String.valueOf(itemId));
        if (itemJson == null || itemJson.isEmpty()) throw new IllegalStateException("购物车中不存在该商品");

        CartItem cartItem = JsonUtils.jsonToPojo(itemJson, CartItem.class);
        if (cartItem == null) throw new IllegalStateException("购物车数据异常");

        int stock = goodsDao.selectGoodById(itemId).getInventory();
        if (stock < 1) throw new IllegalStateException("商品已售罄");
        if (num > stock) num = stock;

        cartItem.setNum(num);
        cartMap.put(String.valueOf(itemId), JsonUtils.objectToJson(cartItem));
        cartMap.expire(shoppinCarTimeOut, TimeUnit.DAYS);
    }

    @Override
    public Long goSettlement(Map<String, Object> maps) {
        // 以 Redis 购物车为准生成订单。
        // 关键：先同步落库成功，再清理购物车/写订单缓存；否则落库失败会“看起来没生成订单”。
        String userId = maps.get("userId").toString();
        String items = maps.get("items").toString();
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());

        long orderId = IDUtils.genOrderId();

        ArrayList<CartItem> cartItems = new ArrayList<>();
        for (String itemId : items.split(",")) {
            String json = cartMap.get(itemId);
            if (json == null || json.isEmpty()) throw new IllegalStateException("购物车中不存在商品: " + itemId);
            CartItem cartItem = JsonUtils.jsonToPojo(json, CartItem.class);
            if (cartItem == null) throw new IllegalStateException("购物车数据异常: " + itemId);
            cartItem.setId(orderId);
            cartItems.add(cartItem);
        }

        addOrderNormal(cartItems, "");

        String normalOrderKey = NORMAL_USER_ORDER_PREFIX + userId + "_" + orderId;
        RMap<String, String> orderMap = redissonClient.getMap(normalOrderKey, new StringCodec());

        String dumpKey = "dump:" + NORMAL_USER_ORDER_PREFIX + userId + "_" + orderId;
        RMap<String, String> dumpMap = redissonClient.getMap(dumpKey, new StringCodec());

        for (CartItem cartItem : cartItems) {
            String itemId = String.valueOf(cartItem.getItemId());
            String v = JsonUtils.objectToJson(cartItem);
            orderMap.put(itemId, v);
            dumpMap.put(itemId, v);
            cartMap.remove(itemId);
        }
        orderMap.expire(normalOrderTimeOut, TimeUnit.DAYS);
        return orderId;
    }

    @Override
    public void goPay(String userId, String[] orderIds) {
        for (String orderId : orderIds) {
            //获取redis订单信息
            String normalOrderKey = NORMAL_USER_ORDER_PREFIX + userId+"_"+orderId;
            RMap<String, String> map = redissonClient.getMap(normalOrderKey, new StringCodec());

            //获取余额
            YsUser ysUser = userDao.selectById(Long.valueOf(userId));
            if (ysUser == null) {
                throw new IllegalStateException("用户不存在");
            }

            //数据库订单行（同一 orderId 可能多行商品）
            List<YsOrder> ysOrders = ysOrderDao.selectsById(Long.parseLong(orderId));
            if(ysOrders.size()>0 && ysOrders.get(0).getStatus().equals(String.valueOf( OrderStatusEnum.PENDING_PAYMENT.getCode()))){
                BigDecimal totalPay = BigDecimal.ZERO;

                for (YsOrder ysOrder : ysOrders) {
                    BigDecimal lineTotal;
                    if (ysOrder.getTotalAmount() != null) {
                        lineTotal = ysOrder.getTotalAmount();
                    } else if (ysOrder.getUnitPrice() != null && ysOrder.getQuantity() != null) {
                        lineTotal = ysOrder.getUnitPrice().multiply(BigDecimal.valueOf(ysOrder.getQuantity()));
                    } else {
                        YsGoods goods = goodsDao.selectGoodById(ysOrder.getGoodsId());
                        int q = ysOrder.getQuantity() != null ? ysOrder.getQuantity() : 1;
                        lineTotal = goods.getPrice().multiply(BigDecimal.valueOf(q));
                    }

                    totalPay = totalPay.add(lineTotal);
                }

                if (ysUser.getBalance() == null || ysUser.getBalance().compareTo(totalPay) < 0) {
                    throw new IllegalStateException("余额不足，需支付：" + totalPay);
                }

                //扣余额
                ysUser.setBalance(ysUser.getBalance().subtract(totalPay));
                userDao.updateBalanceById(ysUser);

                //新增购物历史记录（按行记录商品）
                for (YsOrder ysOrder : ysOrders) {
                    YsShoppingHistory ysShoppingHistory = new YsShoppingHistory();
                    ysShoppingHistory.setId(Long.valueOf(orderId));
                    ysShoppingHistory.setUserId(Long.valueOf(userId));
                    ysShoppingHistory.setGoodsId(ysOrder.getGoodsId());
                    shoppingHistoryDao.insert(ysShoppingHistory);
                }

                //修改订单状态（同一订单号多行，按 id 更新即可）
                int i = ysOrderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), Long.parseLong(orderId));
                if(i>0){
                    log.info("订单支付状态修改成功！");
                    map.delete();
                    
                    // 同时删除 dump 备份数据
                    String dumpKey = "dump:" + NORMAL_USER_ORDER_PREFIX + userId + "_" + orderId;
                    RMap<String, String> dumpMap = redissonClient.getMap(dumpKey, new StringCodec());
                    dumpMap.delete();
                    log.info("订单支付成功，已清理 Redis 缓存和 dump 备份数据：orderId={}", orderId);
                }
                // 发送邮件购买成功
                jmsTemplate.convertAndSend("mail.queue", orderId);
            }else{
                log.info("补偿订单，触发防悬挂");
                //异步线程还未执行生成订单，支付已经就绪，需要帮他生成订单
                ArrayList<CartItem> items = new ArrayList<>();
                for (String itemdId : map.keySet()) {
                    items.add(JsonUtils.jsonToPojo(map.get(itemdId), CartItem.class));
                }
                addOrderNormal(items,"cpnt");
            }
        }
    }
    @Override
    public void addOrderNormal(ArrayList<CartItem> cartItems,String way) {
        for (CartItem cartItem : cartItems) {
            // 1. 创建订单记录
            if(way.equals("cpnt")){
                //防悬挂
                cartItem.setStatusEnum(OrderStatusEnum.PAID.getCode());
            }else {
                cartItem.setStatusEnum(OrderStatusEnum.PENDING_PAYMENT.getCode());
            }

            ysOrderDao.addOrder(cartItem);

            // 2. 扣减数据库中的商品库存
            int stock = goodsDao.selectGoodById(cartItem.getItemId()).getInventory();
            if(stock>0){
                goodsDao.decreaseStock(cartItem.getItemId(), cartItem.getNum());
            }else{
                log.error("商品已售罄");
                throw new RuntimeException("商品已售罄");
            }
        }
    }
    @Override
    public void addOrderNormalCpnt(String orderId,CartItem item ) {

        List<YsOrder> ysOrders = ysOrderDao.selectsById(Long.valueOf(orderId));
        //返还库存
        int code = OrderStatusEnum.PENDING_PAYMENT.getCode();
        if (ysOrders.size()>0 && ysOrders.get(0).getStatus().equals(String.valueOf( code))) {
            for (YsOrder ysOrder : ysOrders) {
                if(ysOrder.getGoodsId().equals(item.getItemId())){
                    goodsDao.increaseStock(item.getItemId(), item.getNum());
                    //作废订单
                    ysOrderDao.deleteById(ysOrder.getId(),ysOrder.getUserId(),ysOrder.getGoodsId(),OrderStatusEnum.EXPIRECANCELLED.getCode());
                }
            }

        }
    }

    @Override
    public void seckill(String itemId, String userId) {
        //获取秒杀的所有商品信息
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new StringCodec());
        // 检查秒杀活动是否已过期
        if (!map.isExists()) {
            throw new IllegalStateException("秒杀活动已结束");
        }
        //分布式锁来锁住秒杀商品
        RLock lock = redissonClient.getLock(STOCK_LOCK+itemId);
        try {
            // 只设置等待时间，不设置锁持有时间。Redisson 默认会使用看门狗机制，在业务执行期间自动续期。
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    //1、获取秒杀商品信息
                    String itemJson = map.get(itemId);
                    CartItem item = JsonUtils.jsonToPojo(itemJson, CartItem.class);
                    //2、检查库存
                    if (item.getNum() < 1) {
                        throw new IllegalStateException("商品已售罄");
                    }else if(redissonClient.getBucket(USER_ORDER_PREFIX + userId+ "_" + itemId).isExists()){ //            3、判断当前用户是否已经秒杀过
                        throw new IllegalStateException("您已经秒杀过该商品");
                    }else{
                        // 4、Redis中商品库存-1
                        item.setNum(item.getNum() - 1);
                        String updatedItemJson = JsonUtils.objectToJson(item);
                        map.put(itemId,updatedItemJson);
                    }
                    //5、创建订单,redis新增一条记录用户和商品id的数据
                    RBucket<Object> order = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId);
                    item.setUserId(Long.parseLong(userId));
                    item.setStatusEnum(OrderStatusEnum.ORDER_CREATING.getCode());
                    String updatedItemJson = JsonUtils.objectToJson(item);
                    order.set(updatedItemJson);
                    order.expire(orderTimeOut, TimeUnit.MINUTES);
                    //6、发送消息
                    jmsTemplate.convertAndSend("seckill.order.queue", updatedItemJson);
                    return;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    lock.unlock();
                }

            }else{
                throw new IllegalStateException("获取秒杀商品id："+itemId+"锁超时，系统繁忙，请稍后重试！");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("秒杀失败，系统繁忙");
        }

    }

    /**
     * 使用Lua脚本实现原子性秒杀（优化版本）
     * 该方法使用Redis Lua脚本原子性地执行秒杀操作，提升性能和一致性
     *
     * @param itemId 商品ID
     * @param userId 用户ID
     * @return 秒杀结果
     */
    public void seckillWithLua(String itemId, String userId) {
        try {
            // Lua脚本实现原子性秒杀
            String luaScript =
                    "local stockKey = KEYS[1]\n" +
                    "local userOrderKey = KEYS[2]\n" +
                    "local userRecordKey = KEYS[3]\n" +
                    "local itemId = ARGV[1]\n" +
                    "local userId = ARGV[2]\n" +
                    "local orderTimeOut = tonumber(ARGV[3])\n" +
                    "local itemJson = ARGV[4]\n" +
                    "\n" +
                    "-- 检查秒杀活动是否存在\n" +
                    "if redis.call('HEXISTS', stockKey, itemId) == 0 then\n" +
                    "    return redis.error_reply('秒杀活动已结束')\n" +
                    "end\n" +
                    "\n" +
                    "-- 检查用户是否已经秒杀过该商品\n" +
                    "if redis.call('HGET', userOrderKey, itemId) ~= false then\n" +
                    "    return redis.error_reply('您已经秒杀过该商品')\n" +
                    "end\n" +
                    "\n" +
                    "-- 获取商品信息\n" +
                    "local goodsJson = redis.call('HGET', stockKey, itemId)\n" +
                    "if not goodsJson then\n" +
                    "    return redis.error_reply('商品不存在')\n" +
                    "end\n" +
                    "\n" +
                    "-- 获取库存计数key\n" +
                    "local stockCountKey = stockKey .. ':count:' .. itemId\n" +
                    "local stock = tonumber(redis.call('GET', stockCountKey))\n" +
                    "\n" +
                    "-- 如果库存计数不存在，从商品JSON中提取（这里简化处理，实际应传入初始库存）\n" +
                    "if not stock then\n" +
                    "    stock = tonumber(redis.call('HGET', stockKey .. ':stock', itemId) or '0')\n" +
                    "    if stock == nil or stock <= 0 then\n" +
                    "        return redis.error_reply('商品已售罄')\n" +
                    "    end\n" +
                    "    redis.call('SET', stockCountKey, stock)\n" +
                    "end\n" +
                    "\n" +
                    "-- 原子递减库存\n" +
                    "local newStock = tonumber(redis.call('DECR', stockCountKey))\n" +
                    "if newStock < 0 then\n" +
                    "    -- 库存不足，回滚\n" +
                    "    redis.call('INCR', stockCountKey)\n" +
                    "    return redis.error_reply('商品已售罄')\n" +
                    "end\n" +
                    "\n" +
                    "-- 更新商品JSON中的库存\n" +
                    "redis.call('HSET', stockKey, itemId, itemJson)\n" +
                    "\n" +
                    "-- 设置用户订单\n" +
                    "redis.call('HSET', userOrderKey, itemId, itemJson)\n" +
                    "redis.call('EXPIRE', userOrderKey, orderTimeOut * 60)\n" +
                    "\n" +
                    "return redis.status_reply('OK')";

            // 准备执行Lua脚本
            RScript script = redissonClient.getScript(new StringCodec());
            String stockKey = STOCK_PREFIX;
            String userOrderKey = USER_ORDER_PREFIX + userId;
            String userRecordKey = USER_RECORD_PREFIX + userId + ":record";

            // 获取商品信息
            RMap<String, String> map = redissonClient.getMap(stockKey, new StringCodec());
            String itemJson = map.get(itemId);
            CartItem item = JsonUtils.jsonToPojo(itemJson, CartItem.class);

            // 如果是第一次秒杀，初始化库存计数
            String stockCountKey = stockKey + ":count:" + itemId;
            if (!redissonClient.getBucket(stockCountKey).isExists()) {
                redissonClient.getBucket(stockCountKey).set(item.getNum());
            }

            // 构建订单JSON
            item.setUserId(Long.parseLong(userId));
            item.setStatusEnum(OrderStatusEnum.ORDER_CREATING.getCode());
            item.setNum(item.getNum() - 1);
            String updatedItemJson = JsonUtils.objectToJson(item);

            // 执行Lua脚本
            Object result = script.eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.VALUE,
                    Collections.emptyList(),
                    stockKey,
                    userOrderKey,
                    userRecordKey,
                    itemId,
                    userId,
                    String.valueOf(orderTimeOut),
                    updatedItemJson
            );

            if ("OK".equals(result)) {
                // 设置订单过期时间
                RBucket<Object> order = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId);
                order.set(updatedItemJson);
                order.expire(orderTimeOut, TimeUnit.MINUTES);

                // 发送消息
                jmsTemplate.convertAndSend("seckill.order.queue", updatedItemJson);
                return;
            } else {
                throw new IllegalStateException(String.valueOf(result));
            }

        } catch (Exception e) {
            log.error("Lua秒杀失败: {}", e.getMessage(), e);
            throw new IllegalStateException("秒杀失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void addOrder(CartItem cartItem,String way) {
        //创建订单前查询redis订单是否过期，过期则不执行！
        if(!redissonClient.getBucket(USER_ORDER_PREFIX + cartItem.getUserId() + "_" + cartItem.getItemId()).isExists()){
            log.info("订单已过期");
        }else{
            // 1. 创建订单记录
            if(way.equals("cpnt")){
                //防悬挂
                cartItem.setStatusEnum(OrderStatusEnum.PAID.getCode());
            }else {
                cartItem.setStatusEnum(OrderStatusEnum.PENDING_PAYMENT.getCode());
            }


            long orderId = IDUtils.genOrderId();
            cartItem.setId(orderId);
            ysOrderDao.addOrder(cartItem);

            // 2. 扣减数据库中的商品库存
            int stock = goodsDao.selectGoodById(cartItem.getItemId()).getInventory();
            if(stock>0){
                goodsDao.decreaseStock(cartItem.getItemId(), cartItem.getNum());
            }else{
                log.error("商品已售罄");
                throw new RuntimeException("商品已售罄");
            }

            try{
                // 3. 更新Redis中订单状态和订单id
                updateRedisOrderStatus(cartItem.getUserId(), cartItem.getItemId(), OrderStatusEnum.PENDING_PAYMENT,orderId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void addOrderCpnt(CartItem cartItem) {
        log.info("执行回滚机制，返还库存");
        //返还库存
        //锁查
        YsGoods ysGoods = goodsDao.selectGoodById(cartItem.getItemId());
        goodsDao.increaseStock(ysGoods.getId(), cartItem.getNum());
        //删除订单
        ysOrderDao.deleteById(cartItem.getId(),cartItem.getUserId(),cartItem.getItemId(),OrderStatusEnum.EXPIRECANCELLED.getCode());

    }

    private void updateRedisOrderStatus(long userId, long itemId, OrderStatusEnum statusEnum, long orderId) throws JsonProcessingException {
        RBucket<String> order = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId);
        String itemJson = order.get();
        if (itemJson != null && !itemJson.isEmpty()) {
            CartItem item = JsonUtils.jsonToPojo(itemJson, CartItem.class);
            item.setStatusEnum(statusEnum.getCode());
            item.setId(orderId);

            String updatedItemJson = JsonUtils.objectToJson(item);
            order.set(updatedItemJson,order.remainTimeToLive(),TimeUnit.MILLISECONDS);

            // 同时设置dump键（不过期）
            String dumpKey = "dump:" + USER_ORDER_PREFIX + userId + "_" + itemId;
            RBucket<String> dumpBucket = redissonClient.getBucket(dumpKey, new StringCodec());
            dumpBucket.set(updatedItemJson);
        }
    }
    
    @Override
    public void initSeckillItem(String itemId, BigDecimal price, int num, String expireTime) {
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new StringCodec());
        CartItem cartItem = new CartItem(Long.valueOf(itemId), price, num);
        map.put(itemId, JsonUtils.objectToJson(cartItem));
        String stockCountKey = STOCK_PREFIX + ":count:" + itemId;
        redissonClient.getBucket(stockCountKey).set(num);
        LocalDateTime expireDateTime = LocalDateTime.now().plusHours(Long.parseLong(expireTime));
        map.expireAt(expireDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        log.info("初始化秒杀商品成功: itemId={}, stock={}", itemId, num);
    }

    @Override
    public void goSeckillSettlement(String itemId, String userId) throws JsonProcessingException {
        //以redis购物车为准,(存在redis订单存在，但是mysql还未生成)
        RBucket<String> order = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId);
        String cartItemJson = order.get();
        if (cartItemJson == null || cartItemJson.isEmpty()) {
            throw new IllegalStateException("订单不存在，可能已过期");
        }
        // 将JSON字符串转换为CartItem对象
        CartItem cartItem = JsonUtils.jsonToPojo(cartItemJson, CartItem.class);
        //数据库商品价格
        YsGoods goods = goodsDao.selectGoodById(Long.valueOf(itemId));
        if(cartItem.getPrice().compareTo(goods.getPrice())!=0){
            log.error("价格不一致,拒绝下单");
            throw new IllegalStateException("价格不一致,拒绝下单");
        }else{
            //        扣除对应余额(不会有并发问题，秒杀场景只会一次支付一个订单)
            YsUser ysUser = userDao.selectById(Long.valueOf(userId));
            ysUser.setBalance(ysUser.getBalance().subtract(cartItem.getPrice()));
            userDao.updateBalanceById(ysUser);
            //修改订单状态
            int i = ysOrderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), cartItem.getId());
            if(i>0){
                log.info("订单支付状态修改成功！");
                //修改redis订单状态
                cartItem.setStatusEnum(OrderStatusEnum.PAID.getCode());
                order.set(JsonUtils.objectToJson(cartItem));
            }else if(i==0 && cartItem.getStatusEnum()==OrderStatusEnum.ORDER_CREATING.getCode()){
                log.info("补偿订单，触发防悬挂");
                //mq还未执行生成订单，秒杀支付已经就绪，需要帮他生成订单
                cartItem.setNum(1);//秒杀场景都是扣1个
                addOrder(cartItem,"cpnt");
            }
            //新增购物历史记录
            YsShoppingHistory ysShoppingHistory = new YsShoppingHistory();
            ysShoppingHistory.setId(cartItem.getId());
            ysShoppingHistory.setUserId(Long.valueOf(userId));
            ysShoppingHistory.setGoodsId(Long.valueOf(itemId));
            shoppingHistoryDao.insert(ysShoppingHistory);
            //  发送邮件购买成功，地址为多少多少！
            jmsTemplate.convertAndSend("mail.queue", cartItem.getId());
        }
    }

    @Override
    public Object showOrder(Long userId) {
        return ysOrderDao.selectsByUserId(userId);
    }

}
