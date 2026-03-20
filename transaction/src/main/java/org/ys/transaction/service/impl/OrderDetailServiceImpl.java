package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.entity.YsUser;
import org.ys.commens.enums.OrderStatusEnum;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.vo.CartItem;
import org.ys.transaction.service.CartService;
import org.ys.transaction.service.OrderDetailService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单详情服务实现类
 *
 * @author makejava
 * @since 2025-07-16
 */
@Service
@Transactional
public class OrderDetailServiceImpl implements OrderDetailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderDetailServiceImpl.class);

    @Resource
    private YsOrderDao orderDao;

    @Resource
    private YsGoodsDao goodsDao;

    @Resource
    private YsUserDao userDao;

    @Resource
    private CartService cartService;

    @Override
    public CommentResult getOrderDetail(Long orderId) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            // 构建订单详情
            Map<String, Object> orderDetail = new HashMap<>();
            YsOrder order = orders.get(0);
            orderDetail.put("order", order);

            // 查询商品信息
            if (order.getGoodsId() != null) {
                YsGoods goods = goodsDao.selectGoodById(order.getGoodsId());
                orderDetail.put("goods", goods);
            }

            // 查询用户信息
            if (order.getUserId() != null) {
                YsUser user = userDao.selectById(order.getUserId());
                orderDetail.put("user", user);
            }

            // 订单状态枚举
            orderDetail.put("statusEnum", getOrderStatusName(order.getStatus()));

            return CommentResult.success(orderDetail);
        } catch (Exception e) {
            log.error("获取订单详情失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("获取订单详情失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserOrders(Long userId, String status, Integer pageNum, Integer pageSize) {
        try {
            // 分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            // 构建查询条件
            QueryWrapper<YsOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            // 订单状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc("id");

            // 分页查询
            Page<YsOrder> page = new Page<>(pageNum, pageSize);
            IPage<YsOrder> orderPage = orderDao.selectPage(page, queryWrapper);

            // 查询每个订单对应的商品信息
            List<YsOrder> orders = orderPage.getRecords();
            for (YsOrder order : orders) {
                if (order.getGoodsId() != null) {
                    YsGoods goods = goodsDao.selectGoodById(order.getGoodsId());
                    order.setGoods(goods);
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", orderPage.getRecords());
            result.put("total", orderPage.getTotal());
            result.put("pageNum", orderPage.getCurrent());
            result.put("pageSize", orderPage.getSize());
            result.put("pages", orderPage.getPages());

            return CommentResult.success(result);
        } catch (Exception e) {
            log.error("获取用户订单列表失败: userId={}, status={}, error={}", userId, status, e.getMessage(), e);
            return CommentResult.error("获取订单列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult applyRefund(Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            Long goodsId = Long.valueOf(params.get("goodsId").toString());
            String reason = (String) params.get("reason");
            java.math.BigDecimal refundAmount = new java.math.BigDecimal(params.get("refundAmount").toString());

            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态（必须是已支付或已完成）
            if (!order.getStatus().equals(String.valueOf(OrderStatusEnum.PAID.getCode())) &&
                !order.getStatus().equals(String.valueOf(OrderStatusEnum.FINISHED.getCode()))) {
                return CommentResult.error("只有已支付或已完成的订单才能申请退款");
            }

            // 更新订单状态为退款中
            order.setStatus(String.valueOf(OrderStatusEnum.REFUNDING.getCode()));
            order.setRefundReason(reason);
            order.setRefundAmount(refundAmount);
            order.setRefundTime(LocalDateTime.now());
            orderDao.updateById(order);

            // 退还库存
            goodsDao.increaseStock(goodsId, order.getQuantity());

            // 退还余额
            YsUser user = userDao.selectById(order.getUserId());
            user.setBalance(user.getBalance().add(refundAmount));
            userDao.updateBalanceById(user);

            log.info("退款申请成功: orderId={}, goodsId={}, refundAmount={}", 
                orderId, goodsId, refundAmount);
            return CommentResult.success("退款申请已提交");
        } catch (Exception e) {
            log.error("申请退款失败: {}", e.getMessage(), e);
            return CommentResult.error("申请退款失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult cancelRefund(Long orderId) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态（必须是退款中）
            if (!order.getStatus().equals(String.valueOf(OrderStatusEnum.REFUNDING.getCode()))) {
                return CommentResult.error("只有退款中的订单才能取消退款");
            }

            // 更新订单状态为已支付或已完成
            if (order.getShipTime() != null && order.getFinishTime() == null) {
                order.setStatus(String.valueOf(OrderStatusEnum.SHIPPED.getCode()));
            } else if (order.getFinishTime() != null) {
                order.setStatus(String.valueOf(OrderStatusEnum.FINISHED.getCode()));
            } else {
                order.setStatus(String.valueOf(OrderStatusEnum.PAID.getCode()));
            }

            order.setRefundReason(null);
            order.setRefundAmount(null);
            order.setRefundTime(null);
            orderDao.updateById(order);

            // 扣减库存
            if (order.getGoodsId() != null) {
                goodsDao.decreaseStock(order.getGoodsId(), order.getQuantity());
            }

            log.info("取消退款成功: orderId={}", orderId);
            return CommentResult.success("退款已取消");
        } catch (Exception e) {
            log.error("取消退款失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("取消退款失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult confirmReceipt(Long orderId) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态（必须是已发货）
            if (!order.getStatus().equals(String.valueOf(OrderStatusEnum.SHIPPED.getCode()))) {
                return CommentResult.error("只有已发货的订单才能确认收货");
            }

            // 更新订单状态为已完成
            order.setStatus(String.valueOf(OrderStatusEnum.FINISHED.getCode()));
            order.setFinishTime(LocalDateTime.now());
            orderDao.updateById(order);

            log.info("确认收货成功: orderId={}", orderId);
            return CommentResult.success("确认收货成功");
        } catch (Exception e) {
            log.error("确认收货失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("确认收货失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult cancelOrder(Long orderId, String reason) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态（只能取消待支付订单）
            if (!order.getStatus().equals(String.valueOf(OrderStatusEnum.PENDING_PAYMENT.getCode()))) {
                return CommentResult.error("只有待支付的订单才能取消");
            }

            // 更新订单状态为已取消
            order.setStatus(String.valueOf(OrderStatusEnum.EXPIRECANCELLED.getCode()));
            order.setRefundReason(reason);
            orderDao.updateById(order);

            // 退还库存
            if (order.getGoodsId() != null) {
                goodsDao.increaseStock(order.getGoodsId(), order.getQuantity());
            }

            log.info("取消订单成功: orderId={}", orderId);
            return CommentResult.success("订单已取消");
        } catch (Exception e) {
            log.error("取消订单失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("取消订单失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getLogisticsInfo(Long orderId) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单状态
            if (!order.getStatus().equals(String.valueOf(OrderStatusEnum.SHIPPED.getCode())) &&
                !order.getStatus().equals(String.valueOf(OrderStatusEnum.FINISHED.getCode()))) {
                return CommentResult.error("订单尚未发货");
            }

            // 构建物流信息（这里模拟物流轨迹）
            Map<String, Object> logisticsInfo = new HashMap<>();
            logisticsInfo.put("logisticsNo", order.getLogisticsNo());
            logisticsInfo.put("logisticsCompany", order.getLogisticsCompany());
            logisticsInfo.put("shipTime", order.getShipTime());

            // 模拟物流轨迹
            List<Map<String, String>> traces = new java.util.ArrayList<>();
            
            // 根据发货时间生成模拟轨迹
            if (order.getShipTime() != null) {
//                traces.add(createTrace(order.getShipTime(), "您的订单已发货"));
//
//                // 模拟物流中转
//                long shipTime = order.getShipTime().getTime();
//                long currentTime = System.currentTimeMillis();
//
//                if (currentTime - shipTime > 24 * 60 * 60 * 1000) { // 超过1天
//                    Date transitTime = new Date(shipTime + 12 * 60 * 60 * 1000);
//                    traces.add(createTrace(transitTime, "您的订单正在配送中"));
//                }
//
//                if (order.getFinishTime() != null) {
//                    traces.add(createTrace(order.getFinishTime(), "您的订单已签收"));
//                } else if (currentTime - shipTime > 48 * 60 * 60 * 1000) { // 超过2天
//                    traces.add(createTrace(new Date(currentTime), "您的订单正在派送中"));
//                }
            }

            logisticsInfo.put("traces", traces);

            return CommentResult.success(logisticsInfo);
        } catch (Exception e) {
            log.error("查询物流信息失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("查询物流信息失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult reorder(Long userId, Long orderId) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单所属用户
            if (!order.getUserId().equals(userId)) {
                return CommentResult.error("无权操作该订单");
            }

            // 将订单商品加入购物车
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setItemId(order.getGoodsId());
            cartItem.setNum(order.getQuantity());

            cartService.addCart(cartItem);

            log.info("重新购买成功: userId={}, orderId={}", userId, orderId);
            return CommentResult.success("已加入购物车");
        } catch (Exception e) {
            log.error("重新购买失败: userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            return CommentResult.error("重新购买失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult deleteOrder(Long userId, Long orderId) {
        try {
            // 查询订单信息
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            // 检查订单所属用户
            if (!order.getUserId().equals(userId)) {
                return CommentResult.error("无权操作该订单");
            }

            // 检查订单状态（只能删除已完成或已取消的订单）
            if (!order.getStatus().equals(String.valueOf(OrderStatusEnum.FINISHED.getCode())) &&
                !order.getStatus().equals(String.valueOf(OrderStatusEnum.EXPIRECANCELLED.getCode()))) {
                return CommentResult.error("只能删除已完成或已取消的订单");
            }

            // 删除订单
            orderDao.deleteById(order.getId(), userId, order.getGoodsId(), 
                Integer.valueOf(order.getStatus()));

            log.info("删除订单成功: userId={}, orderId={}", userId, orderId);
            return CommentResult.success("订单已删除");
        } catch (Exception e) {
            log.error("删除订单失败: userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            return CommentResult.error("删除订单失败: " + e.getMessage());
        }
    }

    /**
     * 创建物流轨迹
     */
    private Map<String, String> createTrace(Date time, String status) {
        Map<String, String> trace = new HashMap<>();
        trace.put("time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time));
        trace.put("status", status);
        return trace;
    }

    /**
     * 获取订单状态名称
     */
    private String getOrderStatusName(String status) {
        try {
            int statusCode = Integer.parseInt(status);
            for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
                if (statusEnum.getCode() == statusCode) {
                    return statusEnum.getDescription();
                }
            }
        } catch (NumberFormatException e) {
            log.error("解析订单状态失败: status={}", status);
        }
        return "未知状态";
    }
}
