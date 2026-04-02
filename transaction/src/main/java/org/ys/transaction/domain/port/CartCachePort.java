package org.ys.transaction.domain.port;

import org.ys.transaction.domain.vo.CartItem;

import java.util.List;
import java.util.Map;

/**
 * 缓存端口（基础设施实现：Redis/Redisson 等）
 * 领域/应用层只依赖该接口，不关心具体缓存技术与序列化方式。
 */
public interface CartCachePort {

    void upsertCartItem(long userId, CartItem item, long expireDays);

    List<CartItem> listCartItems(long userId, long expireDays);

    CartItem getCartItem(long userId, long itemId);

    void removeCartItem(long userId, long itemId);

    /**
     * 普通订单缓存（orderKey: normal:user:order:{userId}_{orderId}）
     */
    void putNormalOrderItems(String normalOrderKey, Map<String, CartItem> itemsByGoodsId, long expireDays);

    Map<String, CartItem> getNormalOrderItems(String normalOrderKey);

    void deleteKey(String key);

    /**
     * dump 备份：dump:{key}（不过期）
     */
    void putDump(String dumpKey, String field, CartItem item);

    /**
     * dump map 读全量（普通订单 dump 用）
     */
    Map<String, CartItem> getDumpMap(String dumpKey);

    CartItem getDumpBucket(String dumpKey);
}
