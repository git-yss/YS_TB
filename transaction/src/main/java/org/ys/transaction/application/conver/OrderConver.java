package org.ys.transaction.application.conver;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.ys.transaction.Infrastructure.pojo.YsGoods;
import org.ys.transaction.Infrastructure.pojo.YsOrder;
import org.ys.transaction.Infrastructure.pojo.YsUser;

@Mapper
public interface OrderConver {
    OrderConver INSTANCE = Mappers.getMapper(OrderConver.class);

    default YsOrder toOrderPo(org.ys.transaction.domain.entity.YsOrder order) {
        if (order == null) {
            return null;
        }
        YsOrder po = new YsOrder();
        po.setId(order.getId());
        po.setUserId(order.getUserId());
        po.setGoodsId(order.getGoodsId());
        po.setStatus(order.getStatus());
        po.setAddtime(order.getAddTime());
        po.setQuantity(order.getQuantity());
        po.setUnitPrice(order.getUnitPrice());
        po.setTotalAmount(order.getTotalAmount());
        po.setPayMethod(order.getPayMethod());
        po.setLogisticsNo(order.getLogisticsNo());
        po.setLogisticsCompany(order.getLogisticsCompany());
        po.setShipTime(order.getShipTime());
        po.setPayTime(order.getPayTime());
        po.setFinishTime(order.getFinishTime());
        po.setRefundTime(order.getRefundTime());
        po.setRefundReason(order.getRefundReason());
        po.setRefundAmount(order.getRefundAmount());
        return po;
    }

    default YsGoods toGoodsPo(org.ys.transaction.domain.entity.YsGoods goods) {
        if (goods == null) {
            return null;
        }
        YsGoods po = new YsGoods();
        po.setId(goods.getId());
        po.setBrand(goods.getBrand());
        po.setName(goods.getName());
        po.setIntroduce(goods.getIntroduce());
        po.setPrice(goods.getPrice());
        po.setInventory(goods.getInventory());
        po.setImage(goods.getImage());
        po.setCategory(goods.getCategory());
        po.setCategoryId(goods.getCategoryId());
        return po;
    }

    default YsUser toUserPo(org.ys.transaction.domain.entity.YsUser user) {
        if (user == null) {
            return null;
        }
        YsUser po = new YsUser();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPassword(user.getPassword());
        po.setAge(user.getAge());
        po.setSex(user.getSex());
        po.setBalance(user.getBalance());
        po.setEmail(user.getEmail());
        po.setTel(user.getTel());
        po.setStatus(user.getStatus());
        po.setCreateTime(user.getCreateTime());
        return po;
    }
}
