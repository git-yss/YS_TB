package org.ys.shoppingcar.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
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
import org.ys.commens.vo.CartItem;
import org.ys.shoppingcar.CartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class CartServiceimpl implements CartService {
    //秒杀商品key
    private static final String STOCK_PREFIX = "seckill:stock:";
    //用户秒杀结果key
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";
    //用户购物车key
    private static final String SHOP_CAR_PREFIX = "shoppingCar:order:";
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CartServiceimpl.class);

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private YsOrderDao ysOrderDao;

    @Resource
    private YsGoodsDao goodsService;

    @Resource
    private YsUserDao userDao;

    @Resource
    private YsShoppingHistoryDao shoppingHistoryDao;

    @Override
    public CommentResult addCart(CartItem cartItem) {
        try {
            String cartKey = SHOP_CAR_PREFIX + cartItem.getUserId();
            RMap<String, String> map = redissonClient.getMap(cartKey, new org.redisson.client.codec.StringCodec());

            ObjectMapper objectMapper = new ObjectMapper();
            String itemId = String.valueOf(cartItem.getItemId());

            // 检查商品是否已经存在购物车中
            if (map.containsKey(itemId)) {
                String existingItemJson = map.get(itemId);
                CartItem existingItem = objectMapper.readValue(existingItemJson, CartItem.class);
                existingItem.setNum(cartItem.getNum());
                String updatedItemJson = objectMapper.writeValueAsString(existingItem);
                map.put(itemId, updatedItemJson);
            } else {
                String cartItemJson = objectMapper.writeValueAsString(cartItem);
                map.put(itemId, cartItemJson);
            }

            // 30日过期
            map.expire(30, TimeUnit.DAYS);
            return CommentResult.ok();
        } catch (Exception e) {
            log.error("添加购物车失败: {}", e.getMessage(), e);
            return CommentResult.error("添加购物车失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult showCart(Long userId) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<Long, CartItem> cartMap = redissonClient.getMap(cartKey);

        // 刷新过期时间
        cartMap.expire(30, TimeUnit.DAYS);
        return  CommentResult.ok(cartMap);
    }

    @Override
    public CommentResult deleteById(Long itemId, Long userId) {
        try {
            String cartKey = SHOP_CAR_PREFIX + userId;
            RMap<String, String> cartMap = redissonClient.getMap(cartKey, new org.redisson.client.codec.StringCodec());
            cartMap.remove(String.valueOf(itemId));
            return CommentResult.ok();
        } catch (Exception e) {
            log.error("删除购物车商品失败: {}", e.getMessage(), e);
            return CommentResult.error("删除购物车商品失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult goSettlement(List<Map<String, Object>> maps) {
        //以redis购物车为准,
//        根据商品id，用户id清除redis缓存数据，

//        扣除对应余额，
//        发送邮件购买成功，地址为多少多少！；
//        商品表减去对应数量；
//        新增购物历史记录
        return null;
    }

    /**
     * 分布式锁版本（秒杀接口）
     * @param itemId 商品id
     * @param userId 用户id
     * @return
     */
    @Override
    public CommentResult seckill(String itemId, String userId) {
        //获取秒杀的所有商品信息
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new org.redisson.client.codec.StringCodec());
        // 检查秒杀活动是否已过期
        if (!map.isExists()) {
            return CommentResult.error("秒杀活动已结束");
        }
        //分布式锁来锁住秒杀商品
        RLock lock = redissonClient.getLock(STOCK_PREFIX+itemId);
        try {
            // 只设置等待时间，不设置锁持有时间。Redisson 默认会使用看门狗机制，在业务执行期间自动续期。
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    //1、获取秒杀商品信息
                    String itemJson = map.get(itemId);
                    ObjectMapper objectMapper = new ObjectMapper();
                    CartItem item = objectMapper.readValue(itemJson, CartItem.class);
                    //2、检查库存
                    if (item.getNum() < 1) {
                        return CommentResult.error("商品已售罄");
                    }else if(redissonClient.getMap(USER_ORDER_PREFIX + userId, new StringCodec()).containsKey(itemId)){ //            3、判断当前用户是否已经秒杀过
                       return CommentResult.error("您已经秒杀过该商品");
                    }else{
                        //            4、Redis中商品库存-1
                        item.setNum(item.getNum() - 1);
                        ObjectMapper objectMapper1 = new ObjectMapper();
                        String updatedItemJson = objectMapper1.writeValueAsString(item);
                        map.put(itemId,updatedItemJson);
                    }
                    //5、创建订单,redis新增一条记录用户和商品id的数据
                    RMap<String, String> seckillUserMap = redissonClient.getMap(USER_ORDER_PREFIX + userId,new StringCodec());
                    item.setUserId(Long.parseLong(userId));
                    ObjectMapper objectMapper2 = new ObjectMapper();
                    String updatedItemJson = objectMapper2.writeValueAsString(item);
                    seckillUserMap.put(itemId,updatedItemJson);
                    //6、发送消息
                    String json = map.get(String.valueOf(itemId));
                                if (json != null && !json.isEmpty()) {
                                    ObjectMapper objectMapper3 = new ObjectMapper();
                                    CartItem item1 = objectMapper3.readValue(itemJson, CartItem.class);
                                    item1.setNum(1);//数量为1
                                    jmsTemplate.convertAndSend("seckill.order.queue", item);
                                    return CommentResult.ok("秒杀成功");
                                }
                                return CommentResult.error("获取商品信息失败");

//                    String luaScript =
//                            "local stockKey = KEYS[1] \n" +
//                                    "local userOrderKey = KEYS[2] \n" +
//                                    "local itemId = ARGV[1] \n" +
//                                    "-- 1、获取秒杀商品信息 \n" +
//                                    "local itemJson = redis.call('HGET', stockKey, itemId) \n" +
//                                    "-- 检查商品是否存在 \n" +
//                                    "if not itemJson or itemJson == false or itemJson == '' then \n" +
//                                    "    return 'Item not found' \n" +
//                                    "end \n" +
//                                    "-- 检查是否为有效JSON \n" +
//                                    "local item = nil \n" +
//                                    "local success, result = pcall(function() return cjson.decode(itemJson) end) \n" +
//                                    "if not success then \n" +
//                                    "    return 'Invalid JSON format: ' .. tostring(result) \n" +
//                                    "end \n" +
//                                    "item = result \n" +
//                                    "-- 2、检查库存 \n" +
//                                    "if not item.num or item.num < 1 then \n" +
//                                    "    return 'Out of stock' \n" +
//                                    "end \n" +
//                                    "-- 3、判断当前用户是否已经秒杀过 \n" +
//                                    "if redis.call('HEXISTS', userOrderKey, itemId) == 1 then \n" +
//                                    "    return 'Already purchased' \n" +
//                                    "end \n" +
//                                    "-- 4、Redis中商品库存-1 \n" +
//                                    "item.num = item.num - 1 \n" +
//                                    "local updatedItemJson = cjson.encode(item) \n" +
//                                    "redis.call('HSET', stockKey, itemId, updatedItemJson) \n" +
//                                    "-- 5、创建订单,redis新增一条记录用户和商品id的数据 \n" +
//                                    "-- 构建订单项，数量为1 \n" +
//                                    "local orderItem = { \n" +
//                                    "    id = item.id or 0, \n" +
//                                    "    itemId = item.itemId or tonumber(itemId), \n" +
//                                    "    price = item.price or 0, \n" +
//                                    "    num = 1, \n" +
//                                    "    userId = item.userId or 0 \n" +
//                                    "} \n" +
//                                    "-- 设置状态枚举 \n" +
//                                    "orderItem.statusEnum = {code=1, description='生成订单中'} \n" +
//                                    "local orderItemJson = cjson.encode(orderItem) \n" +
//                                    "redis.call('HSET', userOrderKey, itemId, orderItemJson) \n" +
//                                    "return 'Success'";
//
//                    try {
//                        RScript script = redissonClient.getScript();
//                        String result = script.eval(
//                                RScript.Mode.READ_WRITE,
//                                luaScript,
//                                RScript.ReturnType.VALUE,
//                                Arrays.asList(STOCK_PREFIX, USER_ORDER_PREFIX + userId),
//                                itemId
//                        );
//
//                        switch (result) {
//                            case "Success":
//                                //消息队列发送订单
//                                String itemJson = map.get(String.valueOf(itemId));
//                                if (itemJson != null && !itemJson.isEmpty()) {
//                                    ObjectMapper objectMapper = new ObjectMapper();
//                                    CartItem item = objectMapper.readValue(itemJson, CartItem.class);
//                                    item.setNum(1);//数量为1
//                                    jmsTemplate.convertAndSend("seckill.order.queue", item);
//                                    return CommentResult.ok("秒杀成功");
//                                }
//                                return CommentResult.error("获取商品信息失败");
//                            case "Out of stock":
//                                return CommentResult.error("商品已售罄");
//                            case "Already purchased":
//                                return CommentResult.error("您已经秒杀过该商品");
//                            case "Item not found":
//                                return CommentResult.error("秒杀商品不存在");
//                            default:
//                                return CommentResult.error("秒杀失败");
//                        }
//                    } catch (Exception e) {
//                        log.error("秒杀失败: {}", e);
//                        throw new RuntimeException(e);
//                    }
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

    @Override
    public void addOrder(CartItem cartItem) {
        // 1. 创建订单记录
        cartItem.setStatusEnum(OrderStatusEnum.PENDING_PAYMENT);
        long orderId = IDUtils.genItemId();
        cartItem.setId(orderId);
        ysOrderDao.addOrder(cartItem);

        // 2. 扣减数据库中的商品库存
        int stock =goodsService.selectGoodById(cartItem.getItemId()).getInventory();
        if(stock>0){
            goodsService.decreaseStock(cartItem.getItemId(), cartItem.getNum());
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



    private void updateRedisOrderStatus(long userId, long itemId, OrderStatusEnum statusEnum, long orderId) throws JsonProcessingException {
        RMap<String, String> userOrderMap = redissonClient.getMap(USER_ORDER_PREFIX + userId, new org.redisson.client.codec.StringCodec());
        String itemJson = userOrderMap.get(String.valueOf(itemId));
        if (itemJson != null && !itemJson.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            CartItem item = objectMapper.readValue(itemJson, CartItem.class);
            item.setStatusEnum(statusEnum);
            item.setId(orderId);
            String updatedItemJson = objectMapper.writeValueAsString(item);
            userOrderMap.put(String.valueOf(itemId), updatedItemJson);
        }
    }
    /**
     * 初始化秒杀商品并设置过期时间
     * @param itemId 商品ID
     * @param expireTime 过期时间（小时）
     */
    @Override
    public CommentResult initSeckillItem(String itemId, BigDecimal price, int num, String expireTime) {
        try {
            RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new org.redisson.client.codec.StringCodec());
            CartItem cartItem = new CartItem(Long.valueOf(itemId), price, num);
            // 将对象转换为JSON字符串存储
            ObjectMapper objectMapper = new ObjectMapper();
            String cartItemJson = objectMapper.writeValueAsString(cartItem);
            // 设置过期时间
            map.put(itemId, cartItemJson);
            LocalDateTime expireDateTime = LocalDateTime.now().plusHours(Long.parseLong(expireTime)); // 2小时后过期
            map.expireAt(expireDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }catch (Exception e){
            return CommentResult.error("初始化秒杀商品失败");
        }
        return CommentResult.ok();
    }

    @Override
    public CommentResult goSeckillSettlement(String itemId, String userId) throws JsonProcessingException {
        //以redis购物车为准,
        RMap<String, String> seckillUserMap = redissonClient.getMap(USER_ORDER_PREFIX + userId, new org.redisson.client.codec.StringCodec());
        String cartItemJson = seckillUserMap.get(itemId);
        if (cartItemJson == null || cartItemJson.isEmpty()) {
            return CommentResult.error("未找到订单信息");
        }
        // 将JSON字符串转换为CartItem对象
        ObjectMapper objectMapper = new ObjectMapper();
        CartItem cartItem = objectMapper.readValue(cartItemJson, CartItem.class);
        //数据库商品
        YsGoods goods = goodsService.selectGoodById(Long.valueOf(itemId));
        if(cartItem.getPrice().compareTo(goods.getPrice())!=0){
            log.error("价格不一致,请核对价格");
        }else{
            //        扣除对应余额(不会有并发问题，秒杀场景支付只会一次支付一个订单)
            YsUser ysUser = new YsUser();
            ysUser.setId(Long.valueOf(userId));
            ysUser.setBalance(ysUser.getBalance().subtract(cartItem.getPrice()));
            userDao.updateById(ysUser);
            //修改订单状态
            int i = ysOrderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), cartItem.getId());
            if(i>0){
                log.info("订单支付成功！");
            }
             //  发送邮件购买成功，地址为多少多少！；
            jmsTemplate.convertAndSend("mail.queue", cartItem.getId());
             //        新增购物历史记录
            YsShoppingHistory ysShoppingHistory = new YsShoppingHistory();
            ysShoppingHistory.setId(cartItem.getId());
            ysShoppingHistory.setUserId(Long.valueOf(userId));
            ysShoppingHistory.setGoodsId(Long.valueOf(itemId));
            shoppingHistoryDao.insert(ysShoppingHistory);
        }


        return CommentResult.ok();
    }

}
