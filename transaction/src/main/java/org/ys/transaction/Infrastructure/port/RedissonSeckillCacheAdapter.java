package org.ys.transaction.Infrastructure.port;

import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.ys.transaction.Infrastructure.utils.JsonUtils;
import org.ys.transaction.domain.port.SeckillCachePort;
import org.ys.transaction.domain.vo.CartItem;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedissonSeckillCacheAdapter implements SeckillCachePort {

    private static final String STOCK_PREFIX = "seckill:stock";
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean stockActivityExists() {
        return redissonClient.getMap(STOCK_PREFIX, new StringCodec()).isExists();
    }

    @Override
    public CartItem getStockItem(String itemId) {
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX, new StringCodec());
        String json = map.get(itemId);
        return json == null || json.isEmpty() ? null : JsonUtils.jsonToPojo(json, CartItem.class);
    }

    @Override
    public void putStockItem(String itemId, CartItem item) {
        RMap<String, String> map = redissonClient.getMap(STOCK_PREFIX, new StringCodec());
        map.put(itemId, JsonUtils.objectToJson(item));
    }

    @Override
    public boolean userOrderExists(String userId, String itemId) {
        return redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId).isExists();
    }

    @Override
    public void putUserOrder(String userId, String itemId, CartItem order, long expireMinutes) {
        RBucket<String> bucket = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId, new StringCodec());
        bucket.set(JsonUtils.objectToJson(order));
        bucket.expire(expireMinutes, TimeUnit.MINUTES);
    }

    @Override
    public CartItem getUserOrder(String userId, String itemId) {
        RBucket<String> bucket = redissonClient.getBucket(USER_ORDER_PREFIX + userId + "_" + itemId, new StringCodec());
        String json = bucket.get();
        return json == null || json.isEmpty() ? null : JsonUtils.jsonToPojo(json, CartItem.class);
    }

    @Override
    public void putDumpUserOrder(String userId, String itemId, CartItem order) {
        String dumpKey = "dump:" + USER_ORDER_PREFIX + userId + "_" + itemId;
        RBucket<String> dump = redissonClient.getBucket(dumpKey, new StringCodec());
        dump.set(JsonUtils.objectToJson(order));
    }

    @Override
    public CartItem getDumpUserOrder(String userId, String itemId) {
        String dumpKey = "dump:" + USER_ORDER_PREFIX + userId + "_" + itemId;
        RBucket<String> dump = redissonClient.getBucket(dumpKey, new StringCodec());
        String json = dump.get();
        return json == null || json.isEmpty() ? null : JsonUtils.jsonToPojo(json, CartItem.class);
    }

    @Override
    public Map<String, CartItem> getDumpNormalOrder(String dumpKey) {
        RMap<String, String> dumpMap = redissonClient.getMap(dumpKey, new StringCodec());
        Map<String, CartItem> result = new HashMap<>();
        for (String k : dumpMap.keySet()) {
            String v = dumpMap.get(k);
            if (v == null || v.isEmpty()) continue;
            CartItem item = JsonUtils.jsonToPojo(v, CartItem.class);
            if (item != null) result.put(k, item);
        }
        return result;
    }
}


