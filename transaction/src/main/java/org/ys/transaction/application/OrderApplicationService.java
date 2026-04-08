package org.ys.transaction.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.application.conver.OrderConver;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.respository.YsOrderRespository;
import org.ys.transaction.domain.service.OrderCommandService;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderApplicationService {

    @Resource
    private YsOrderRespository ysOrderRespository;

    @Resource
    private OrderCommandService orderCommandService;

    public List<OrderAggregate> list(Long userId) {

        List<OrderAggregate> aggregates = ysOrderRespository.selectsByUserId(
                new OrderAggregate(YsOrder.rehydrate(null, userId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null),
                        null, null, null)
        );
        return aggregates;

    }

    public Map<String, Object> detail(Long orderId, Long userId) {
        OrderAggregate orderAggregate = getOrderAggregate(orderId);
        orderAggregate.checkBelongTo(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", OrderConver.INSTANCE.toOrderPo(orderAggregate.getOrder()));
        result.put("goods", OrderConver.INSTANCE.toGoodsPo(orderAggregate.getGoods()));
        result.put("user", OrderConver.INSTANCE.toUserPo(orderAggregate.getUser()));
        result.put("details", java.util.Collections.emptyList());
        result.put("statusDesc", orderAggregate.getStatusDesc());
        return result;
    }

    @Transactional
    public void cancel(Long orderId, Long userId, String reason) {
        orderCommandService.cancel(orderId, userId, reason);
    }

    @Transactional
    public void confirmReceipt(Long orderId, Long userId) {
        orderCommandService.confirmReceipt(orderId, userId);
    }

    @Transactional
    public void applyRefund(Long orderId, Long userId, String refundReason) {
        orderCommandService.applyRefund(orderId, userId, refundReason);
    }

    @Transactional
    public void handleRefund(Long orderId, Boolean approve, String refundReason) {
        orderCommandService.handleRefund(orderId, approve, refundReason);
    }

    @Transactional
    public void updateLogistics(Long orderId, String logisticsNo, String logisticsCompany) {
        orderCommandService.updateLogistics(orderId, logisticsNo, logisticsCompany);
    }

    @Transactional
    public void shipOrder(Long orderId, String logisticsNo, String logisticsCompany) {
        orderCommandService.shipOrder(orderId, logisticsNo, logisticsCompany);
    }

    private OrderAggregate getOrderAggregate(Long orderId) {
        OrderAggregate aggregate = ysOrderRespository.selectAggregateById(
                new OrderAggregate(YsOrder.identify(orderId), null, null, null)
        );
        if (aggregate == null || aggregate.getOrder() == null) {
            throw new IllegalStateException("订单不存在");
        }
        return aggregate;
    }

    
}
