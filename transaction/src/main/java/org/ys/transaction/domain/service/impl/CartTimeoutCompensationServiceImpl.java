package org.ys.transaction.domain.service.impl;

import org.springframework.stereotype.Service;
import org.ys.transaction.domain.aggregate.GoodsAggregate;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.enums.OrderStatusEnum;
import org.ys.transaction.domain.port.CartCachePort;
import org.ys.transaction.domain.port.DistributedLockPort;
import org.ys.transaction.domain.port.SeckillCachePort;
import org.ys.transaction.domain.respository.YsGoodsRespository;
import org.ys.transaction.domain.respository.YsOrderRespository;
import org.ys.transaction.domain.service.CartTimeoutCompensationService;
import org.ys.transaction.domain.vo.CartItem;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CartTimeoutCompensationServiceImpl implements CartTimeoutCompensationService {

    private static final String STOCK_LOCK = "seckill:lock:itemId:";
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";
    private static final String NORMAL_USER_ORDER_PREFIX = "normal:user:order:";

    @Resource
    private SeckillCachePort seckillCachePort;

    @Resource
    private CartCachePort cartCachePort;

    @Resource
    private DistributedLockPort distributedLockPort;

    @Resource
    private YsGoodsRespository ysGoodsRespository;

    @Resource
    private YsOrderRespository ysOrderRespository;

    @Override
    public void onRedisKeyExpired(String expiredKey) {
        if (expiredKey == null) return;
        if (expiredKey.startsWith(USER_ORDER_PREFIX)) {
            handleSeckillExpiry(expiredKey);
        } else if (expiredKey.startsWith(NORMAL_USER_ORDER_PREFIX)) {
            handleNormalOrderExpiry(expiredKey);
        }
    }

    private void handleSeckillExpiry(String orderKey) {
        // seckill:user:order:{userId}_{itemId}
        String[] parts = orderKey.split("[:_]");
        if (parts.length < 5) return;
        String userId = parts[3];
        String itemId = parts[4];

        CartItem expiredOrder = seckillCachePort.getDumpUserOrder(userId, itemId);
        if (expiredOrder == null) return;

        if (!seckillCachePort.stockActivityExists()) {
            cartCachePort.deleteKey("dump:" + orderKey);
            return;
        }

        distributedLockPort.withLock(STOCK_LOCK + itemId, 100, TimeUnit.MILLISECONDS, () -> {
            CartItem stock = seckillCachePort.getStockItem(itemId);
            if (stock != null) {
                stock.setNum((stock.getNum() == null ? 0 : stock.getNum()) + 1);
                seckillCachePort.putStockItem(itemId, stock);
            }

            // 删除数据库订单、返还数据库库存
            CartItem item = expiredOrder;
            item.setNum(1);
            rollbackSeckillDbOrder(item);

            cartCachePort.deleteKey("dump:" + orderKey);
            return null;
        });
    }

    private void rollbackSeckillDbOrder(CartItem item) {
        // 返还库存
        GoodsAggregate goodsAggregate = ysGoodsRespository.selectGoodById(
                new GoodsAggregate(null, YsGoods.identify(item.getItemId()), null)
        );
        YsGoods goods = goodsAggregate == null ? null : goodsAggregate.getGoods();
        if (goods != null) {
            ysGoodsRespository.increaseStock(new GoodsAggregate(
                    YsOrder.rehydrate(null, null, null, null, null, item.getNum(), null, null, null, null, null, null, null, null, null, null, null),
                    YsGoods.identify(goods.getId()),
                    null
            ));
        }
        // 作废订单
        ysOrderRespository.deleteById(new OrderAggregate(
                YsOrder.rehydrate(item.getId(), item.getUserId(), item.getItemId(), String.valueOf(OrderStatusEnum.EXPIRECANCELLED.getCode()),
                        null, null, null, null, null, null, null, null, null, null, null, null, null),
                null, null, null
        ));
    }

    private void handleNormalOrderExpiry(String expiredKey) {
        // normal:user:order:{userId}_{orderId}
        String dumpKey = "dump:" + expiredKey;
        Map<String, CartItem> items = cartCachePort.getDumpMap(dumpKey);
        String[] parts = expiredKey.split("[:_]");
        if (parts.length < 5) return;
        String orderId = parts[4];

        for (CartItem item : items.values()) {
            rollbackNormalDbOrder(orderId, item);
        }
        cartCachePort.deleteKey(dumpKey);
    }

    private void rollbackNormalDbOrder(String orderId, CartItem item) {
        // 返还库存 + 作废订单（按 goodsId 精准作废）
        int code = OrderStatusEnum.PENDING_PAYMENT.getCode();
        java.util.List<OrderAggregate> ysOrders = ysOrderRespository.selectsById(
                new OrderAggregate(YsOrder.identify(Long.valueOf(orderId)), null, null, null)
        );
        if (ysOrders.size() > 0 && ysOrders.get(0).getOrder().getStatus().equals(String.valueOf(code))) {
            for (OrderAggregate orderAggregate : ysOrders) {
                YsOrder ysOrder = orderAggregate.getOrder();
                if (ysOrder.getGoodsId().equals(item.getItemId())) {
                    ysGoodsRespository.increaseStock(new GoodsAggregate(
                            YsOrder.rehydrate(null, null, null, null, null, item.getNum(), null, null, null, null, null, null, null, null, null, null, null),
                            YsGoods.identify(item.getItemId()),
                            null
                    ));
                    ysOrderRespository.deleteById(new OrderAggregate(
                            YsOrder.rehydrate(ysOrder.getId(), ysOrder.getUserId(), ysOrder.getGoodsId(), String.valueOf(OrderStatusEnum.EXPIRECANCELLED.getCode()),
                                    null, null, null, null, null, null, null, null, null, null, null, null, null),
                            null, null, null
                    ));
                }
            }
        }
    }
}

