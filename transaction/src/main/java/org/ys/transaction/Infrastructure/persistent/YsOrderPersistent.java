package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.Infrastructure.conver.OrderConver;
import org.ys.transaction.Infrastructure.pojo.*;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.aggregate.PayAggregate;
import org.ys.transaction.domain.respository.YsOrderRespository;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.dao.YsOrderDao;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.dao.YsUserAddrDao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class YsOrderPersistent implements YsOrderRespository {
    private final YsOrderDao ysOrderDao;
    private final YsGoodsDao ysGoodsDao;
    private final YsUserDao ysUserDao;
    private final YsUserAddrDao ysUserAddrDao;

    @Override
    public int insertBatch(List<OrderAggregate> entities) {
        List<YsOrder> orders = entities.stream().map(e -> toPo(e.getOrder())).collect(Collectors.toList());
        return ysOrderDao.insertBatch(orders);
    }

    @Override
    public int insertOrUpdateBatch(List<OrderAggregate> entities) {
        List<YsOrder> orders = entities.stream().map(e -> toPo(e.getOrder())).collect(Collectors.toList());
        return ysOrderDao.insertOrUpdateBatch(orders);
    }

    @Override
    public int addOrder(OrderAggregate aggregate) {
        YsOrder po = toPo(aggregate.getOrder());
        return ysOrderDao.insert(po);
    }

    @Override
    public int updateById(OrderAggregate aggregate) {
        return ysOrderDao.updateById(toPo(aggregate.getOrder()));
    }

    @Override
    public int updateStatusById(OrderAggregate aggregate) {
        Integer code = aggregate.getOrder().getStatus() == null ? 0 : Integer.parseInt(aggregate.getOrder().getStatus());
        return ysOrderDao.updateStatusById(code, aggregate.getOrder().getId());
    }

    @Override
    public int deleteById(OrderAggregate aggregate) {
        return ysOrderDao.deleteById(
                aggregate.getOrder().getId(),
                aggregate.getOrder().getUserId() == null ? 0 : aggregate.getOrder().getUserId(),
                aggregate.getOrder().getGoodsId() == null ? 0 : aggregate.getOrder().getGoodsId(),
                aggregate.getOrder().getStatus() == null ? 0 : Integer.parseInt(aggregate.getOrder().getStatus())
        );
    }

    @Override
    public List<OrderAggregate> selectsById(OrderAggregate aggregate) {
        return ysOrderDao.selectsById(aggregate.getOrder().getId()).stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderAggregate> selectsByUserId(OrderAggregate aggregate) {
        return ysOrderDao.selectsByUserId(aggregate.getOrder().getUserId()).stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderAggregate> selectDetailById(long orderId) {
        List<Order> ysOrders = ysOrderDao.selectDetailById(orderId);
        ArrayList<OrderAggregate> list = new ArrayList<>();
        for (Order order : ysOrders) {
            // 注意：这里需要根据实际情况获取其他实体
            // 由于 Order 已经包含了关联查询的数据，建议手动构建聚合根
            YsOrder ysOrder = convertToYsOrder(order);
            // 从数据库中查询商品、用户和地址信息
            YsGoods goodsPo = ysGoodsDao.selectById(ysOrder.getGoodsId());
            YsUser userPo = ysUserDao.selectById(ysOrder.getUserId());
            YsUserAddr addrPo = ysUserAddrDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<YsUserAddr>()
                    .eq("user_id", ysOrder.getUserId())
            );
            
            // 将 POJO 转换为领域实体
            org.ys.transaction.domain.entity.YsGoods goods = goodsPo == null ? null : 
                org.ys.transaction.domain.entity.YsGoods.rehydrate(
                    goodsPo.getId(), goodsPo.getBrand(), goodsPo.getName(), goodsPo.getIntroduce(), 
                    goodsPo.getPrice(), goodsPo.getInventory(), goodsPo.getImage(), 
                    goodsPo.getCategory(), goodsPo.getCategoryId()
                );
            
            org.ys.transaction.domain.entity.YsUser user = userPo == null ? null : 
                toUserEntity(userPo);
            
            org.ys.transaction.domain.entity.YsUserAddr addr = addrPo == null ? null :
                org.ys.transaction.domain.entity.YsUserAddr.rehydrate(
                    addrPo.getId(), addrPo.getUserId(), addrPo.getAddr()
                );
            
            list.add(OrderConver.INSTANCE.poToAggregate(order, goods, user, addr));
        }
        return list;
    }




    @Override
    public OrderAggregate selectAggregateById(OrderAggregate aggregate) {
        List<YsOrder> rows = ysOrderDao.selectsById(aggregate.getOrder().getId());
        YsOrder order = (rows == null || rows.isEmpty()) ? null : rows.get(0);
        if (order == null) return null;
        return toAggregate(order);
    }

    @Override
    public PayAggregate selectPayAggregateById(PayAggregate aggregate) {
        List<YsOrder> rows = ysOrderDao.selectsById(aggregate.getOrder().getId());
        YsOrder orderPo = (rows == null || rows.isEmpty()) ? null : rows.get(0);
        if (orderPo == null) return null;
        YsUser userPo = ysUserDao.selectById(orderPo.getUserId());
        if (userPo == null) return null;
        return new PayAggregate(toOrderEntity(orderPo), toUserEntity(userPo));
    }

    private OrderAggregate toAggregate(YsOrder po) {
        YsGoods goodsPo = ysGoodsDao.selectById(po.getGoodsId());
        YsUser userPo = ysUserDao.selectById(po.getUserId());
        return new OrderAggregate(
                toOrderEntity(po),
                goodsPo == null ? null : org.ys.transaction.domain.entity.YsGoods.rehydrate(
                        goodsPo.getId(), goodsPo.getBrand(), goodsPo.getName(), goodsPo.getIntroduce(), goodsPo.getPrice(),
                        goodsPo.getInventory(), goodsPo.getImage(), goodsPo.getCategory(), goodsPo.getCategoryId()),
                userPo == null ? null : toUserEntity(userPo),null
        );
    }


    private org.ys.transaction.domain.entity.YsOrder toOrderEntity(YsOrder po) {
        return org.ys.transaction.domain.entity.YsOrder.rehydrate(
                po.getId(), po.getUserId(), po.getGoodsId(), po.getStatus(), po.getAddtime(), po.getQuantity(),
                po.getUnitPrice(), po.getTotalAmount(), po.getPayMethod(), po.getLogisticsNo(), po.getLogisticsCompany(),
                po.getShipTime(), po.getPayTime(), po.getFinishTime(), po.getRefundTime(), po.getRefundReason(), po.getRefundAmount()
        );
    }

    private org.ys.transaction.domain.entity.YsUser toUserEntity(YsUser po) {
        return org.ys.transaction.domain.entity.YsUser.rehydrate(
                po.getId(), po.getUsername(), po.getPassword(), po.getAge(), po.getSex(),
                po.getBalance(), po.getEmail(), po.getTel(), po.getStatus(), po.getCreateTime()
        );
    }

    private YsOrder toPo(org.ys.transaction.domain.entity.YsOrder e) {
        YsOrder po = new YsOrder();
        po.setId(e.getId());
        po.setUserId(e.getUserId());
        po.setGoodsId(e.getGoodsId());
        po.setStatus(e.getStatus());
        po.setAddtime(e.getAddTime());
        po.setQuantity(e.getQuantity());
        po.setUnitPrice(e.getUnitPrice());
        po.setTotalAmount(e.getTotalAmount());
        po.setPayMethod(e.getPayMethod());
        po.setLogisticsNo(e.getLogisticsNo());
        po.setLogisticsCompany(e.getLogisticsCompany());
        po.setShipTime(e.getShipTime());
        po.setPayTime(e.getPayTime());
        po.setFinishTime(e.getFinishTime());
        po.setRefundTime(e.getRefundTime());
        po.setRefundReason(e.getRefundReason());
        po.setRefundAmount(e.getRefundAmount());
        return po;
    }

    /**
     * 将 Order POJO 转换为 YsOrder 实体
     */
    private YsOrder convertToYsOrder(Order order) {
        if (order == null) {
            return null;
        }
        
        YsOrder ysOrder = new YsOrder();
        // 如果需要设置 ID，可以在这里解析
        // ysOrder.setId(Long.parseLong(order.getOrderId()));
        
        return ysOrder;
    }
}
