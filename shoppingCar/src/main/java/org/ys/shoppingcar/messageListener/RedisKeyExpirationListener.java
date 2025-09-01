package org.ys.shoppingcar.messageListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.ys.commens.enums.OrderStatusEnum;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.vo.CartItem;
import org.ys.shoppingcar.CartService;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisKeyExpirationListener implements MessageListener {

    //秒杀商品key
    private static final String STOCK_PREFIX = "seckill:stock";

    //秒杀获得分布式锁前缀
    private static final String STOCK_LOCK = "seckill:lock:itemId:";

    private static final Logger logger = LoggerFactory.getLogger(RedisKeyExpirationListener.class);

    //用户秒杀订单key
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";

    @Resource
    private CartService cartService;

    @Resource
    private RedissonClient redissonClient;
    @Override
    public void onMessage(CharSequence channel, Object message) {
        String expiredKey = message.toString();
        logger.warn("监听到Redis键过期: {}", expiredKey);
        // 根据键的前缀执行不同的业务逻辑
        if (expiredKey.startsWith(USER_ORDER_PREFIX)) {
            handleOrderExpiry(expiredKey);
        }
        // 可以添加更多条件分支处理不同类型的键
    }

    private void handleOrderExpiry(String orderKey) {
       //解析key中的userId和itemId “seckill:user:order:123_321”
        String[] parts = orderKey.split("[:_]");
        String userId = parts[3]; // 123
        String itemId = parts[4]; // 321
       //获取秒杀的所有商品信息
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX,new StringCodec());
        // 检查秒杀活动是否已过期
        if (!map.isExists()) {

        }else{
            RBucket<String> order = redissonClient.getBucket(orderKey);

            // 库存+1
            //分布式锁来锁住秒杀商品
            RLock lock = redissonClient.getLock(STOCK_LOCK+itemId);
            try {
                // 只设置等待时间，不设置锁持有时间。Redisson 默认会使用看门狗机制，在业务执行期间自动续期。
                if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                    try {
                        //1、获取秒杀商品订单
                        String orderJson  = order.get();
                        ObjectMapper objectMapper = new ObjectMapper();
                        CartItem item = objectMapper.readValue(orderJson, CartItem.class);
                        //1、获取秒杀商品信息,库存+1
                        String itemJson = map.get(itemId);
                        CartItem itemInfo = objectMapper.readValue(itemJson, CartItem.class);
                        itemInfo.setNum(itemInfo.getNum() + 1);
                        String updatedItemJson = objectMapper.writeValueAsString(itemInfo);
                        map.put(itemId,updatedItemJson);
                        //2、删除订单，释放库存
                        item.setNum(1);
                        cartService.addOrderCpnt(item);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }finally {
                        lock.unlock();
                    }

                }else{
                    throw new RuntimeException("获取秒杀商品id："+itemId+"锁超时，系统繁忙，请稍后重试！");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                 throw new RuntimeException(e);
            }finally {
                lock.unlock();
            }
        }
    }


}
