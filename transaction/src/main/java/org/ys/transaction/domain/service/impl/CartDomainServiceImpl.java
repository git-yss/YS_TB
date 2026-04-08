package org.ys.transaction.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ys.transaction.domain.aggregate.GoodsAggregate;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.aggregate.ShoppingHistoryAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsShoppingHistory;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.enums.OrderStatusEnum;
import org.ys.transaction.domain.port.CartCachePort;
import org.ys.transaction.domain.port.DistributedLockPort;
import org.ys.transaction.domain.port.EventPublisherPort;
import org.ys.transaction.domain.port.IdGeneratorPort;
import org.ys.transaction.domain.port.SeckillCachePort;
import org.ys.transaction.domain.respository.YsGoodsRespository;
import org.ys.transaction.domain.respository.YsOrderRespository;
import org.ys.transaction.domain.respository.YsShoppingHistoryRespository;
import org.ys.transaction.domain.respository.YsUserRespository;
import org.ys.transaction.domain.service.CartDomainService;
import org.ys.transaction.domain.vo.CartItem;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartDomainServiceImpl implements CartDomainService {

    private static final String STOCK_LOCK = "seckill:lock:itemId:";
    private static final String NORMAL_USER_ORDER_PREFIX = "normal:user:order:";

    @Resource
    private CartCachePort cartCachePort;

    @Resource
    private SeckillCachePort seckillCachePort;

    @Resource
    private DistributedLockPort distributedLockPort;

    @Resource
    private EventPublisherPort eventPublisherPort;

    @Resource
    private IdGeneratorPort idGeneratorPort;

    @Resource
    private YsOrderRespository ysOrderRespository;

    @Resource
    private YsGoodsRespository ysGoodsRespository;

    @Resource
    private YsUserRespository ysUserRespository;

    @Resource
    private YsShoppingHistoryRespository ysShoppingHistoryRespository;

    @Value("${seckill.orderTimeOut}")
    private long orderTimeOut;

    @Value("${normal.orderTimeOut}")
    private long normalOrderTimeOut;

    @Value("${shoppinCar.timeOut}")
    private long shoppinCarTimeOut;

    @Override
    public void addCart(CartItem item) {
        if (item == null) throw new IllegalArgumentException("参数错误");
        if (item.getUserId() <= 0 || item.getItemId() <= 0) throw new IllegalArgumentException("参数错误");
        if (item.getNum() == null || item.getNum() < 1) throw new IllegalArgumentException("数量必须大于等于1");
        cartCachePort.upsertCartItem(item.getUserId(), item, shoppinCarTimeOut);
    }

    @Override
    public List<CartItem> showCart(Long userId) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("参数错误");
        return cartCachePort.listCartItems(userId, shoppinCarTimeOut);
    }

    @Override
    public void deleteById(Long itemId, Long userId) {
        if (itemId == null || userId == null) throw new IllegalArgumentException("参数错误");
        cartCachePort.removeCartItem(userId, itemId);
    }

    @Override
    public void updateCartNum(Long itemId, Long userId, Integer num) {
        if (itemId == null || userId == null) throw new IllegalArgumentException("参数错误");
        if (num == null || num < 1) throw new IllegalArgumentException("数量必须大于等于1");

        CartItem cartItem = cartCachePort.getCartItem(userId, itemId);
        if (cartItem == null) throw new IllegalStateException("购物车中不存在该商品");

        GoodsAggregate goodsAggregate = ysGoodsRespository.selectGoodById(
                new GoodsAggregate(null, YsGoods.identify(itemId), null)
        );
        YsGoods goods = goodsAggregate == null ? null : goodsAggregate.getGoods();
        if (goods == null) throw new IllegalStateException("商品不存在");

        int stock = goods.getInventory() == null ? 0 : goods.getInventory();
        if (stock < 1) throw new IllegalStateException("商品已售罄");
        if (num > stock) num = stock;

        cartItem.setNum(num);
        cartCachePort.upsertCartItem(userId, cartItem, shoppinCarTimeOut);
    }

    @Override
    public Long goSettlement(Map<String, Object> items) {
        String userId = items.get("userId").toString();
        String goodsItems = items.get("items").toString();

        long orderId = idGeneratorPort.nextOrderId();

        Map<String, CartItem> selected = new HashMap<>();
        for (String itemId : goodsItems.split(",")) {
            CartItem cartItem = cartCachePort.getCartItem(Long.parseLong(userId), Long.parseLong(itemId));
            if (cartItem == null) throw new IllegalStateException("购物车中不存在商品: " + itemId);
            cartItem.setId(orderId);
            selected.put(itemId, cartItem);
        }

        addOrderNormal(selected.values().stream().collect(Collectors.toList()), "");

        String normalOrderKey = NORMAL_USER_ORDER_PREFIX + userId + "_" + orderId;
        cartCachePort.putNormalOrderItems(normalOrderKey, selected, normalOrderTimeOut);
        String dumpKey = "dump:" + normalOrderKey;
        for (Map.Entry<String, CartItem> e : selected.entrySet()) {
            cartCachePort.putDump(dumpKey, e.getKey(), e.getValue());
            cartCachePort.removeCartItem(Long.parseLong(userId), Long.parseLong(e.getKey()));
        }

        return orderId;
    }

    @Override
    public void goPay(String userId, String[] orderIds) {
        for (String orderId : orderIds) {
            UserAggregate userAggregate = ysUserRespository.selectAggregateById(userId);
            YsUser ysUser = userAggregate == null ? null : userAggregate.getUser();
            if (ysUser == null) throw new IllegalStateException("用户不存在");

            List<OrderAggregate> ysOrders = ysOrderRespository.selectsById(
                    new OrderAggregate(YsOrder.identify(Long.parseLong(orderId)), null, null, null)
            );
            if (ysOrders.isEmpty()) throw new IllegalStateException("订单不存在");

            if (!String.valueOf(OrderStatusEnum.PENDING_PAYMENT.getCode()).equals(ysOrders.get(0).getOrder().getStatus())) {
                throw new IllegalStateException("订单状态不支持支付");
            }

            BigDecimal totalPay = BigDecimal.ZERO;
            for (OrderAggregate orderAggregate : ysOrders) {
                YsOrder ysOrder = orderAggregate.getOrder();
                BigDecimal lineTotal = ysOrder.getTotalAmount();
                if (lineTotal == null && ysOrder.getUnitPrice() != null && ysOrder.getQuantity() != null) {
                    lineTotal = ysOrder.getUnitPrice().multiply(BigDecimal.valueOf(ysOrder.getQuantity()));
                }
                if (lineTotal == null) throw new IllegalStateException("订单金额异常");
                totalPay = totalPay.add(lineTotal);
            }

            BigDecimal balance = ysUser.getBalance() == null ? BigDecimal.ZERO : ysUser.getBalance();
            if (balance.compareTo(totalPay) < 0) throw new IllegalStateException("余额不足，需支付：" + totalPay);

            YsUser paidUser = YsUser.rehydrate(
                    ysUser.getId(), ysUser.getUsername(), ysUser.getPassword(), ysUser.getAge(), ysUser.getSex(),
                    balance.subtract(totalPay), ysUser.getEmail(), ysUser.getTel(), ysUser.getStatus(), ysUser.getCreateTime()
            );
            ysUserRespository.updateBalanceById(new UserAggregate(paidUser, null));

            for (OrderAggregate orderAggregate : ysOrders) {
                YsOrder ysOrder = orderAggregate.getOrder();
                ysShoppingHistoryRespository.insert(new ShoppingHistoryAggregate(
                        YsShoppingHistory.rehydrate(Long.valueOf(orderId), Long.valueOf(userId), ysOrder.getGoodsId())
                ));
            }

            ysOrderRespository.updateStatusById(new OrderAggregate(
                    YsOrder.rehydrate(Long.parseLong(orderId), null, null, String.valueOf(OrderStatusEnum.PAID.getCode()),
                            null, null, null, null, null, null, null, null, null, null, null, null, null),
                    null, null, null
            ));

            cartCachePort.deleteKey(NORMAL_USER_ORDER_PREFIX + userId + "_" + orderId);
            cartCachePort.deleteKey("dump:" + NORMAL_USER_ORDER_PREFIX + userId + "_" + orderId);

            eventPublisherPort.publish("mail.queue", orderId);
        }
    }

    @Override
    public void goSeckillSettlement(String itemId, String userId) throws JsonProcessingException {
        CartItem cartItem = seckillCachePort.getUserOrder(userId, itemId);
        if (cartItem == null) throw new IllegalStateException("订单不存在，可能已过期");

        GoodsAggregate goodsAggregate = ysGoodsRespository.selectGoodById(
                new GoodsAggregate(null, YsGoods.identify(Long.valueOf(itemId)), null)
        );
        YsGoods goods = goodsAggregate == null ? null : goodsAggregate.getGoods();
        if (goods == null) throw new IllegalStateException("商品不存在");
        if (cartItem.getPrice() == null || goods.getPrice() == null || cartItem.getPrice().compareTo(goods.getPrice()) != 0) {
            throw new IllegalStateException("价格不一致,拒绝下单");
        }

        UserAggregate userAggregate = ysUserRespository.selectAggregateById(userId);
        YsUser ysUser = userAggregate == null ? null : userAggregate.getUser();
        if (ysUser == null) throw new IllegalStateException("用户不存在");
        if (ysUser.getBalance() == null || ysUser.getBalance().compareTo(cartItem.getPrice()) < 0) throw new IllegalStateException("余额不足");

        YsUser paidUser = YsUser.rehydrate(
                ysUser.getId(), ysUser.getUsername(), ysUser.getPassword(), ysUser.getAge(), ysUser.getSex(),
                ysUser.getBalance().subtract(cartItem.getPrice()), ysUser.getEmail(), ysUser.getTel(), ysUser.getStatus(), ysUser.getCreateTime()
        );
        ysUserRespository.updateBalanceById(new UserAggregate(paidUser, null));

        ysOrderRespository.updateStatusById(new OrderAggregate(
                YsOrder.rehydrate(cartItem.getId(), null, null, String.valueOf(OrderStatusEnum.PAID.getCode()),
                        null, null, null, null, null, null, null, null, null, null, null, null, null),
                null, null, null
        ));

        ysShoppingHistoryRespository.insert(new ShoppingHistoryAggregate(
                YsShoppingHistory.rehydrate(cartItem.getId(), Long.valueOf(userId), Long.valueOf(itemId))
        ));
        eventPublisherPort.publish("mail.queue", cartItem.getId());
    }

    @Override
    public void seckill(String itemId, String userId) {
        if (!seckillCachePort.stockActivityExists()) {
            throw new IllegalStateException("秒杀活动已结束");
        }

        distributedLockPort.withLock(STOCK_LOCK + itemId, 100, TimeUnit.MILLISECONDS, () -> {
            CartItem stock = seckillCachePort.getStockItem(itemId);
            if (stock == null) throw new IllegalStateException("商品不存在");
            if (stock.getNum() == null || stock.getNum() < 1) throw new IllegalStateException("商品已售罄");
            if (seckillCachePort.userOrderExists(userId, itemId)) throw new IllegalStateException("您已经秒杀过该商品");

            stock.setNum(stock.getNum() - 1);
            seckillCachePort.putStockItem(itemId, stock);

            CartItem order = new CartItem();
            order.setItemId(Long.parseLong(itemId));
            order.setUserId(Long.parseLong(userId));
            order.setNum(1);
            order.setPrice(stock.getPrice());
            order.setStatusEnum(OrderStatusEnum.ORDER_CREATING.getCode());

            seckillCachePort.putUserOrder(userId, itemId, order, orderTimeOut);
            seckillCachePort.putDumpUserOrder(userId, itemId, order);

            eventPublisherPort.publish("seckill.order.queue", order);
            return null;
        });
    }

    @Override
    public void initSeckillItem(String itemId, BigDecimal price, int num, String expireTime) {
        CartItem stock = new CartItem(Long.parseLong(itemId), price, num);
        seckillCachePort.putStockItem(itemId, stock);
        // expireAt 属于缓存实现细节：此处略过（保持与现有行为一致，建议后续在 SeckillCachePort 增加 expireAt 能力）
    }

    private void addOrderNormal(List<CartItem> cartItems, String way) {
        for (CartItem cartItem : cartItems) {
            cartItem.setStatusEnum(way.equals("cpnt") ? OrderStatusEnum.PAID.getCode() : OrderStatusEnum.PENDING_PAYMENT.getCode());
            ysOrderRespository.addOrder(new OrderAggregate(toOrderEntity(cartItem), null, null, null));

            GoodsAggregate goodsAggregate = ysGoodsRespository.selectGoodById(
                    new GoodsAggregate(null, YsGoods.identify(cartItem.getItemId()), null)
            );
            YsGoods goods = goodsAggregate == null ? null : goodsAggregate.getGoods();
            if (goods == null) throw new IllegalStateException("商品不存在");
            if (goods.getInventory() == null || goods.getInventory() < cartItem.getNum()) throw new IllegalStateException("商品已售罄");

            ysGoodsRespository.decreaseStock(new GoodsAggregate(
                    YsOrder.rehydrate(null, null, null, null, null, cartItem.getNum(), null, null, null, null, null, null, null, null, null, null, null),
                    YsGoods.identify(cartItem.getItemId()),
                    null
            ));
        }
    }

    private YsOrder toOrderEntity(CartItem cartItem) {
        return YsOrder.rehydrate(
                cartItem.getId(),
                cartItem.getUserId(),
                cartItem.getItemId(),
                String.valueOf(cartItem.getStatusEnum()),
                null,
                cartItem.getNum(),
                cartItem.getPrice(),
                cartItem.getPrice() == null ? null : cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getNum())),
                null, null, null, null, null, null, null, null, null
        );
    }
}

