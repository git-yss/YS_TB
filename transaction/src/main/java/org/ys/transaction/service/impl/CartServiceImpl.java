package org.ys.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsShoppingHistoryDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.entity.YsShoppingHistory;
import org.ys.commens.entity.YsUser;
import org.ys.commens.enums.OrderStatusEnum;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.utils.IDUtils;
import org.ys.commens.utils.JsonUtils;
import org.ys.commens.vo.CartItem;
import org.ys.transaction.service.CartService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
    public CommentResult addCart(CartItem cartItem) {
        try {
            String cartKey = SHOP_CAR_PREFIX + cartItem.getUserId();
            RMap<String, String> map = redissonClient.getMap(cartKey, new StringCodec());
            String itemId = String.valueOf(cartItem.getItemId());

            // 检查商品是否已经存在购物车中
            if (map.containsKey(itemId)) {
                String existingItemJson = map.get(itemId);
                CartItem existingItem = JsonUtils.jsonToPojo(existingItemJson, CartItem.class);
                existingItem.setNum(existingItem.getNum() + cartItem.getNum());
                String updatedItemJson = JsonUtils.objectToJson(existingItem);
                map.put(itemId, updatedItemJson);
            } else {
                String cartItemJson = JsonUtils.objectToJson(cartItem);
                map.put(itemId, cartItemJson);
            }

            // 30日过期
            map.expire(shoppinCarTimeOut, TimeUnit.DAYS);
            return CommentResult.ok();
        } catch (Exception e) {
            log.error("添加购物车失败: {}", e.getMessage(), e);
            return CommentResult.error("添加购物车失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult showCart(Long userId) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey);

        // 刷新过期时间
        cartMap.expire(shoppinCarTimeOut, TimeUnit.DAYS);
        return  CommentResult.ok(cartMap);
    }

    @Override
    public CommentResult deleteById(Long itemId, Long userId) {
        try {
            String cartKey = SHOP_CAR_PREFIX + userId;
            RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
            cartMap.remove(String.valueOf(itemId));
            return CommentResult.ok();
        } catch (Exception e) {
            log.error("删除购物车商品失败: {}", e.getMessage(), e);
            return CommentResult.error("删除购物车商品失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult goSettlement( Map<String, Object> maps) {
        //以redis购物车为准,生成订单，订单过期时间设置为7天
        String userId = maps.get("userId").toString();
        String items = maps.get("items").toString();
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey,new StringCodec());
        long orderId = IDUtils.genOrderId();

        String normalOrderKey = NORMAL_USER_ORDER_PREFIX + userId+"_"+orderId;
        RMap<String, String> map = redissonClient.getMap(normalOrderKey, new StringCodec());
        // 同时设置dump键（不过期）,用作过期后获取值的备用key
        String dumpKey = "dump:" + NORMAL_USER_ORDER_PREFIX + userId+"_"+orderId;
        RMap<String, String> dumpMap = redissonClient.getMap(dumpKey, new StringCodec());
        ArrayList<CartItem> cartItems = new ArrayList<>();
        for (String itemId : items.split(",")) {
            CartItem cartItem = JsonUtils.jsonToPojo(cartMap.get(itemId), CartItem.class);
            cartItem.setId(orderId);
            cartItems.add(cartItem);
            map.put(itemId, JsonUtils.objectToJson(cartItem));
            dumpMap.put(itemId, JsonUtils.objectToJson(cartItem));
            //将购物车中相关的信息清除掉
            cartMap.remove(itemId);
        }
        map.expire(normalOrderTimeOut, TimeUnit.DAYS);
        //使用线程池异步执行添加数据库普通订单，扣库存
        CompletableFuture.runAsync(()->{
            try {
                addOrderNormal(cartItems,"");
            } catch (Exception e) {
                log.error("添加普通订单失败: {}", e.getMessage(), e);
                //走补偿机制 清理redis购物车
                RMap<String, String> carMap = redissonClient.getMap(normalOrderKey, new StringCodec());
                carMap.delete();
            }
        });

        return CommentResult.ok(orderId);
    }

    @Override
    public CommentResult goPay(String userId, String[] orderIds) {
        for (String orderId : orderIds) {
            //获取redis订单信息
            String normalOrderKey = NORMAL_USER_ORDER_PREFIX + userId+"_"+orderId;
            RMap<String, String> map = redissonClient.getMap(normalOrderKey, new StringCodec());

            //获取余额
            YsUser ysUser = userDao.selectById(Long.valueOf(userId));
            //数据库商品价格
            List<YsOrder> ysOrders = ysOrderDao.selectsById(Long.parseLong(orderId));
            if(ysOrders.size()>0 && ysOrders.get(0).getStatus().equals(String.valueOf( OrderStatusEnum.PENDING_PAYMENT.getCode()))){
                for (YsOrder ysOrder : ysOrders) {
                    String itemId = String.valueOf(ysOrder.getGoodsId());
                    YsGoods goods = goodsDao.selectGoodById(Long.valueOf(itemId));
                    BigDecimal price = goods.getPrice();
                    ysUser.setBalance(ysUser.getBalance().subtract(price));

                    //新增购物历史记录
                    YsShoppingHistory ysShoppingHistory = new YsShoppingHistory();
                    ysShoppingHistory.setId(Long.valueOf(orderId));
                    ysShoppingHistory.setUserId(Long.valueOf(userId));
                    ysShoppingHistory.setGoodsId(Long.valueOf(itemId));
                    shoppingHistoryDao.insert(ysShoppingHistory);

                }
                // 扣除对应余额(不会有并发问题，只会一次支付一个订单)
                userDao.updateBalanceById(ysUser);
                //修改订单状态
                int i = ysOrderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), Long.parseLong(orderId));
                if(i>0){
                    log.info("订单支付状态修改成功！");
                    map.delete();
                }
                //  发送邮件购买成功，地址为多少多少！
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
        return CommentResult.ok();
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
    public CommentResult seckill(String itemId, String userId) {
        //获取秒杀的所有商品信息
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new StringCodec());
        // 检查秒杀活动是否已过期
        if (!map.isExists()) {
            return CommentResult.error("秒杀活动已结束");
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
                        return CommentResult.error("商品已售罄");
                    }else if(redissonClient.getBucket(USER_ORDER_PREFIX + userId+ "_" + itemId).isExists()){ //            3、判断当前用户是否已经秒杀过
                        return CommentResult.error("您已经秒杀过该商品");
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
                    return CommentResult.ok("秒杀成功");

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    lock.unlock();
                }

            }else{
                return CommentResult.error("获取秒杀商品id："+itemId+"锁超时，系统繁忙，请稍后重试！");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CommentResult.error("秒杀失败，系统繁忙");
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
    public CommentResult seckillWithLua(String itemId, String userId) {
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
                return CommentResult.ok("秒杀成功");
            } else {
                return CommentResult.error(result.toString());
            }

        } catch (Exception e) {
            log.error("Lua秒杀失败: {}", e.getMessage(), e);
            return CommentResult.error("秒杀失败: " + e.getMessage());
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
    public CommentResult initSeckillItem(String itemId, BigDecimal price, int num, String expireTime) {
        try {
            RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new StringCodec());
            CartItem cartItem = new CartItem(Long.valueOf(itemId), price, num);
            // 将对象转换为JSON字符串存储
            String cartItemJson = JsonUtils.objectToJson(cartItem);
            // 设置过期时间
            map.put(itemId, cartItemJson);
            // 初始化库存计数（用于Lua脚本原子操作）
            String stockCountKey = STOCK_PREFIX + ":count:" + itemId;
            redissonClient.getBucket(stockCountKey).set(num);
            LocalDateTime expireDateTime = LocalDateTime.now().plusHours(Long.parseLong(expireTime));
            map.expireAt(expireDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            log.info("初始化秒杀商品成功: itemId={}, stock={}", itemId, num);
        }catch (Exception e){
            log.error("初始化秒杀商品失败: {}", e.getMessage(), e);
            return CommentResult.error("初始化秒杀商品失败");
        }
        return CommentResult.ok();
    }

    @Override
    public CommentResult goSeckillSettlement(String itemId, String userId) throws JsonProcessingException {
        //以redis购物车为准,(存在redis订单存在，但是mysql还未生成)
        RBucket<String> order = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId);
        String cartItemJson = order.get();
        if (cartItemJson == null || cartItemJson.isEmpty()) {
            return CommentResult.error("订单不存在，可能已过期");
        }
        // 将JSON字符串转换为CartItem对象
        CartItem cartItem = JsonUtils.jsonToPojo(cartItemJson, CartItem.class);
        //数据库商品价格
        YsGoods goods = goodsDao.selectGoodById(Long.valueOf(itemId));
        if(cartItem.getPrice().compareTo(goods.getPrice())!=0){
            log.error("价格不一致,拒绝下单");
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


        return CommentResult.ok();
    }

    @Override
    public CommentResult showOrder(Long userId) {
        return CommentResult.ok(ysOrderDao.selectsByUserId(userId));
    }

}
