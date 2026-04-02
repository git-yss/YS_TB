package org.ys.transaction.domain.port;

import org.ys.transaction.domain.vo.CartItem;

import java.util.Map;

/**
 * 秒杀缓存端口（基础设施实现：Redis/Redisson 等）
 */
public interface SeckillCachePort {

    boolean stockActivityExists();

    CartItem getStockItem(String itemId);

    void putStockItem(String itemId, CartItem item);

    boolean userOrderExists(String userId, String itemId);

    void putUserOrder(String userId, String itemId, CartItem order, long expireMinutes);

    CartItem getUserOrder(String userId, String itemId);

    /**
     * dump:{seckill:user:order:{userId}_{itemId}}
     */
    void putDumpUserOrder(String userId, String itemId, CartItem order);

    CartItem getDumpUserOrder(String userId, String itemId);

    Map<String, CartItem> getDumpNormalOrder(String dumpKey);
}

