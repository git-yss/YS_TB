package org.ys.transaction.Infrastructure.conver;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.ys.transaction.Infrastructure.pojo.Order;
import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.entity.YsUserAddr;

@Mapper
public interface OrderConver {
    OrderConver INSTANCE = Mappers.getMapper(OrderConver.class);

    /**
     * 将 Order POJO 转换为 OrderAggregate
     * 注意：由于 OrderAggregate 需要多个实体对象，需要使用多参数方法
     */
    default OrderAggregate poToAggregate(Order order, YsGoods goods, YsUser user, YsUserAddr addr) {
        if (order == null) {
            return null;
        }
        
        // 将 Order 转换为 YsOrder
        YsOrder ysOrder = convertToYsOrder(order);
        
        // 构建聚合根
        return new OrderAggregate(ysOrder, goods, user, addr);
    }
    
    /**
     * 将 Order 转换为 YsOrder
     * 这里可以根据实际需求进行转换
     */
    default YsOrder convertToYsOrder(Order order) {
        if (order == null) {
            return null;
        }
        
        YsOrder ysOrder = new YsOrder();
        // 如果需要设置具体 ID，可以这样：
        // ysOrder.setId(Long.parseLong(order.getOrderId()));
        
        // 根据实际需求映射其他字段
        // 注意：Order 类的字段可能与 YsOrder 不完全对应，需要手动处理
        
        return ysOrder;
    }
}
