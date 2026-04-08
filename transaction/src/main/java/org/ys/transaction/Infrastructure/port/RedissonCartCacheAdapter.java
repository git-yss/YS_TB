package org.ys.transaction.Infrastructure.port;

import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.ys.transaction.Infrastructure.utils.JsonUtils;
import org.ys.transaction.domain.port.CartCachePort;
import org.ys.transaction.domain.vo.CartItem;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedissonCartCacheAdapter implements CartCachePort {

    private static final String SHOP_CAR_PREFIX = "shoppingCar:order:";

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void upsertCartItem(long userId, CartItem item, long expireDays) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> map = redissonClient.getMap(cartKey, new StringCodec());
        String itemId = String.valueOf(item.getItemId());

        String existing = map.get(itemId);
        if (existing != null && !existing.isEmpty()) {
            CartItem existingItem = JsonUtils.jsonToPojo(existing, CartItem.class);
            existingItem.setNum(existingItem.getNum() + item.getNum());
            map.put(itemId, JsonUtils.objectToJson(existingItem));
        } else {
            map.put(itemId, JsonUtils.objectToJson(item));
        }
        map.expire(expireDays, TimeUnit.DAYS);
    }

    @Override
    public List<CartItem> listCartItems(long userId, long expireDays) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
        cartMap.expire(expireDays, TimeUnit.DAYS);
        return cartMap.readAllValues().stream()
                .filter(v -> v != null && !v.isEmpty())
                .map(v -> JsonUtils.jsonToPojo(v, CartItem.class))
                .filter(v -> v != null)
                .collect(Collectors.toList());
    }

    @Override
    public CartItem getCartItem(long userId, long itemId) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
        String v = cartMap.get(String.valueOf(itemId));
        return v == null || v.isEmpty() ? null : JsonUtils.jsonToPojo(v, CartItem.class);
    }

    @Override
    public void removeCartItem(long userId, long itemId) {
        String cartKey = SHOP_CAR_PREFIX + userId;
        RMap<String, String> cartMap = redissonClient.getMap(cartKey, new StringCodec());
        cartMap.remove(String.valueOf(itemId));
    }

    @Override
    public void putNormalOrderItems(String normalOrderKey, Map<String, CartItem> itemsByGoodsId, long expireDays) {
        RMap<String, String> orderMap = redissonClient.getMap(normalOrderKey, new StringCodec());
        for (Map.Entry<String, CartItem> e : itemsByGoodsId.entrySet()) {
            orderMap.put(e.getKey(), JsonUtils.objectToJson(e.getValue()));
        }
        orderMap.expire(expireDays, TimeUnit.DAYS);
    }

    @Override
    public Map<String, CartItem> getNormalOrderItems(String normalOrderKey) {
        RMap<String, String> map = redissonClient.getMap(normalOrderKey, new StringCodec());
        Map<String, CartItem> result = new HashMap<>();
        for (String k : map.keySet()) {
            String v = map.get(k);
            if (v == null || v.isEmpty()) continue;
            CartItem item = JsonUtils.jsonToPojo(v, CartItem.class);
            if (item != null) result.put(k, item);
        }
        return result;
    }

    @Override
    public void deleteKey(String key) {
        redissonClient.getBucket(key, new StringCodec()).delete();
    }

    @Override
    public void putDump(String dumpKey, String field, CartItem item) {
        RMap<String, String> dumpMap = redissonClient.getMap(dumpKey, new StringCodec());
        dumpMap.put(field, JsonUtils.objectToJson(item));
    }

    @Override
    public Map<String, CartItem> getDumpMap(String dumpKey) {
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

    @Override
    public CartItem getDumpBucket(String dumpKey) {
        RBucket<String> dumpBucket = redissonClient.getBucket(dumpKey, new StringCodec());
        String v = dumpBucket.get();
        return v == null || v.isEmpty() ? null : JsonUtils.jsonToPojo(v, CartItem.class);
    }
}


