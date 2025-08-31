package org.ys.shoppingcar.impl;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
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
        String cartKey = SHOP_CAR_PREFIX + cartItem.getUserId();
        RMap<Long, CartItem> map = redissonClient.getMap(cartKey);
        //检查商品是否已经存在购物车中
        if(map.containsKey(cartItem.getItemId())){
            CartItem goods = map.get(cartItem.getItemId());
            goods.setNum(cartItem.getNum());
            map.put(cartItem.getItemId(),goods);
        }else{
            map.put(cartItem.getItemId(),cartItem);
        }
        //30日过期
        map.expire(30,TimeUnit.DAYS);
        return CommentResult.ok();
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
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<Long, CartItem> cartMap = redissonClient.getMap(cartKey);
        cartMap.remove(itemId);
        return  CommentResult.ok();
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
    public CommentResult seckill(Long itemId, Long userId) {
        //获取秒杀的所有商品信息
        RMap<Long, CartItem> map = redissonClient.getMap(STOCK_PREFIX);
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
//                    //1、获取秒杀商品信息
//                    CartItem item = map.get(STOCK_PREFIX+itemId);
//                    //2、检查库存
//                    if (item.getNum() < 1) {
//                        return CommentResult.error("商品已售罄");
//                    }else if(redissonClient.getMap(USER_ORDER_PREFIX + userId).containsKey(itemId)){ //            3、判断当前用户是否已经秒杀过
//                       return CommentResult.error("您已经秒杀过该商品");
//                    }else{
//                        //            4、Redis中商品库存-1
//                        item.setNum(item.getNum() - 1);
//                        map.put(STOCK_PREFIX+itemId,item);
//                    }
//                    //5、创建订单,redis新增一条记录用户和商品id的数据
//                    RMap<String, CartItem> seckillUserMap = redissonClient.getMap(USER_ORDER_PREFIX + userId);
//                    seckillUserMap.put(itemId,item);

                    String luaScript =
                            "local stockKey = KEYS[1] \n" +
                                    "local userOrderKey = KEYS[2] \n" +
                                    "local itemId = ARGV[1] \n" +
                                    "-- 1、获取秒杀商品信息 \n" +
                                    "local itemJson = redis.call('HGET', stockKey, itemId) \n" +
                                    "-- 检查商品是否存在 \n" +
                                    "if not itemJson then \n" +
                                    "    return 'Item not found' \n" +
                                    "end \n" +
                                    "-- 反序列化商品信息 \n" +
                                    "local item = cjson.decode(itemJson) \n" +
                                    "-- 2、检查库存 \n" +
                                    "if item.num < 1 then \n" +
                                    "    return 'Out of stock' \n" +
                                    "end \n" +
                                    "-- 3、判断当前用户是否已经秒杀过 \n" +
                                    "if redis.call('HEXISTS', userOrderKey, itemId) == 1 then \n" +
                                    "    return 'Already purchased' \n" +
                                    "end \n" +
                                    "-- 4、Redis中商品库存-1 \n" +
                                    "item.num = item.num - 1 \n" +
                                    "redis.call('HSET', stockKey, itemId, cjson.encode(item)) \n" +
                                    "-- 5、创建订单,redis新增一条记录用户和商品id的数据 \n" +
                                    "item.statusEnum = {code=1, description='生成订单中'} \n" +
                                    "redis.call('HSET', userOrderKey, itemId, cjson.encode(item)) \n" +
                                    "return 'Success'";

                    try {
                        RScript script = redissonClient.getScript();
                        String result = script.eval(
                                RScript.Mode.READ_WRITE,
                                luaScript,
                                RScript.ReturnType.VALUE,
                                Arrays.asList(STOCK_PREFIX, USER_ORDER_PREFIX + userId),
                                itemId
                        );

                        switch (result) {
                            case "Success":
                                //消息队列发送订单
                                CartItem item = map.get(STOCK_PREFIX+itemId);
                                item.setNum(1);//数量为1
                                jmsTemplate.convertAndSend("seckill.order.queue", item);
                                return CommentResult.ok("秒杀成功");
                            case "Out of stock":
                                return CommentResult.error("商品已售罄");
                            case "Already purchased":
                                return CommentResult.error("您已经秒杀过该商品");
                            case "Item not found":
                                return CommentResult.error("秒杀商品不存在");
                            default:
                                return CommentResult.error("秒杀失败");
                        }
                    } catch (Exception e) {
                        return CommentResult.error("秒杀失败: " + e.getMessage());
                    }
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



    private void updateRedisOrderStatus(long userId, long itemId, OrderStatusEnum statusEnum, long orderId) {
        RMap<Long, CartItem> userOrderMap = redissonClient.getMap(USER_ORDER_PREFIX + userId);
        CartItem item = userOrderMap.get(itemId);
        if (item != null) {
            item.setStatusEnum(statusEnum);
            item.setId(orderId);
            userOrderMap.put(itemId, item);
        }
    }
    /**
     * 初始化秒杀商品并设置过期时间
     * @param itemId 商品ID
     * @param expireTime 过期时间（小时）
     */
    @Override
    public CommentResult initSeckillItem(long itemId, BigDecimal price, int num, long expireTime) {
        try {
            RMap<Long, CartItem> map = redissonClient.getMap(STOCK_PREFIX);
            CartItem cartItem = new CartItem(itemId, price, num);
            // 设置过期时间
            map.put(itemId, cartItem);
            LocalDateTime expireDateTime = LocalDateTime.now().plusHours(expireTime); // 2小时后过期
            map.expireAt(expireDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }catch (Exception e){
            return CommentResult.error("初始化秒杀商品失败");
        }
        return CommentResult.ok();
    }

    @Override
    public CommentResult goSeckillSettlement(Long itemId, Long userId) {
        //以redis购物车为准,
        RMap<String, CartItem> seckillUserMap = redissonClient.getMap(USER_ORDER_PREFIX + userId);
        CartItem cartItem = seckillUserMap.get(itemId);

        //数据库商品
        YsGoods goods = goodsService.selectGoodById(itemId);
        if(cartItem.getPrice().compareTo(goods.getPrice())!=0){
            log.error("价格不一致,请核对价格");
        }else{
            //        扣除对应余额(不会有并发问题，秒杀场景支付只会一次支付一个订单)
            YsUser ysUser = new YsUser();
            ysUser.setId(userId);
            ysUser.setBalance(ysUser.getBalance().subtract(cartItem.getPrice()));
            userDao.updateById(ysUser);
            //修改订单状态
            YsOrder order = ysOrderDao.selectById(cartItem.getId());
            int i = ysOrderDao.updateStatusById(OrderStatusEnum.PAID.getCode(), cartItem.getId());
            if(i>0){
                log.info("订单支付成功！");
            }

             //  发送邮件购买成功，地址为多少多少！；

            jmsTemplate.convertAndSend("mail.queue", cartItem.getId());
             //        新增购物历史记录
            YsShoppingHistory ysShoppingHistory = new YsShoppingHistory();
            ysShoppingHistory.setId(cartItem.getId());
            ysShoppingHistory.setUserId(userId);
            ysShoppingHistory.setGoodsId(itemId);
            shoppingHistoryDao.insert(ysShoppingHistory);
        }


        return CommentResult.ok();
    }

}
