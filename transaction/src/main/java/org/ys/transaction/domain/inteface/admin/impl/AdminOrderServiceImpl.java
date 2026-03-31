package org.ys.transaction.domain.inteface.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.entity.YsUser;
import org.ys.transaction.domain.vo.DomainResult;
import org.ys.transaction.domain.inteface.admin.AdminOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台订单管理服务实现
 */
@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    @Resource
    private YsOrderDao ysOrderDao;

    @Resource
    private YsUserDao ysUserDao;

    @Resource
    private YsGoodsDao ysGoodsDao;

    @Override
    public DomainResult getOrderList(String status, Long userId, String keyword, Integer pageNum, Integer pageSize) {
        try {
            Page<YsOrder> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();

            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", status);
            }

            if (userId != null) {
                wrapper.eq("user_id", userId);
            }

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.eq("id", keyword);
            }

            wrapper.orderByDesc("addtime");
            IPage<YsOrder> pageResult = ysOrderDao.selectPage(page, wrapper);

            List<YsOrder> orderList = pageResult.getRecords();
            for (YsOrder order : orderList) {
                YsUser user = ysUserDao.selectById(order.getUserId());
                if (user != null) {
                    order.setUsername(user.getUsername());
                }

                YsGoods goods = ysGoodsDao.selectById(order.getGoodsId());
                if (goods != null) {
                    order.setGoodsName(goods.getName());
                    order.setGoodsImage(goods.getImage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", orderList);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", pageResult.getCurrent());
            result.put("pageSize", pageResult.getSize());

            return DomainResult.success(result);
        } catch (Exception e) {
            return DomainResult.error("获取订单列表失败：" + e.getMessage());
        }
    }

    @Override
    public DomainResult getOrderDetail(Long id) {
        try {
            YsOrder order = ysOrderDao.selectById(id);
            if (order == null) {
                return DomainResult.error("订单不存在");
            }

            YsUser user = ysUserDao.selectById(order.getUserId());
            if (user != null) {
                order.setUsername(user.getUsername());
                order.setUserTel(user.getTel());
            }

            YsGoods goods = ysGoodsDao.selectById(order.getGoodsId());
            if (goods != null) {
                order.setGoodsName(goods.getName());
                order.setGoodsImage(goods.getImage());
            }

            return DomainResult.success(order);
        } catch (Exception e) {
            return DomainResult.error("获取订单详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DomainResult shipOrder(Long orderId, String logisticsCompany, String logisticsNo) {
        try {
            YsOrder order = ysOrderDao.selectById(orderId);
            if (order == null) {
                return DomainResult.error("订单不存在");
            }

            if (!"1".equals(order.getStatus())) {
                return DomainResult.error("订单状态不正确，无法发货");
            }

            UpdateWrapper<YsOrder> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", orderId);
            wrapper.set("status", "2");
            wrapper.set("logistics_company", logisticsCompany);
            wrapper.set("logistics_no", logisticsNo);
            wrapper.set("ship_time", LocalDateTime.now());

            int count = ysOrderDao.update(null, wrapper);
            if (count > 0) {
                return DomainResult.success("发货成功");
            }
            return DomainResult.error("发货失败");
        } catch (Exception e) {
            return DomainResult.error("发货失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DomainResult batchShipOrder(List<Map<String, Object>> orderList) {
        try {
            int successCount = 0;
            for (Map<String, Object> orderData : orderList) {
                Long orderId = Long.parseLong(orderData.get("orderId").toString());
                String logisticsCompany = orderData.get("logisticsCompany") != null ? orderData.get("logisticsCompany").toString() : null;
                String logisticsNo = orderData.get("logisticsNo") != null ? orderData.get("logisticsNo").toString() : null;

                DomainResult result = shipOrder(orderId, logisticsCompany, logisticsNo);
                if (result.isSuccess()) {
                    successCount++;
                }
            }
            return DomainResult.success("批量发货成功，共发货 " + successCount + " 单");
        } catch (Exception e) {
            return DomainResult.error("批量发货失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DomainResult refundOrder(Long orderId, String refundReason) {
        try {
            YsOrder order = ysOrderDao.selectById(orderId);
            if (order == null) {
                return DomainResult.error("订单不存在");
            }

            if (!"4".equals(order.getStatus())) {
                return DomainResult.error("订单状态不正确，无法退款");
            }

            YsUser user = ysUserDao.selectById(order.getUserId());
            if (user == null) {
                return DomainResult.error("用户不存在");
            }

            UpdateWrapper<YsOrder> orderWrapper = new UpdateWrapper<>();
            orderWrapper.eq("id", orderId);
            orderWrapper.set("status", "5");
            orderWrapper.set("refund_time", LocalDateTime.now());
            orderWrapper.set("refund_reason", refundReason);
            orderWrapper.set("refund_amount", order.getTotalAmount());
            ysOrderDao.update(null, orderWrapper);

            YsUser updateUser = new YsUser();
            updateUser.setId(order.getUserId());
            updateUser.setBalance(user.getBalance().add(order.getTotalAmount()));
            ysUserDao.updateById(updateUser);

            UpdateWrapper<YsGoods> goodsWrapper = new UpdateWrapper<>();
            goodsWrapper.eq("id", order.getGoodsId());
            goodsWrapper.setSql("inventory = inventory + " + order.getQuantity());
            ysGoodsDao.update(null, goodsWrapper);

            return DomainResult.success("退款成功");
        } catch (Exception e) {
            return DomainResult.error("退款失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DomainResult cancelOrder(Long id) {
        try {
            YsOrder order = ysOrderDao.selectById(id);
            if (order == null) {
                return DomainResult.error("订单不存在");
            }

            UpdateWrapper<YsOrder> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id);
            wrapper.set("status", "6");
            ysOrderDao.update(null, wrapper);

            return DomainResult.success("取消订单成功");
        } catch (Exception e) {
            return DomainResult.error("取消订单失败：" + e.getMessage());
        }
    }

    @Override
    public DomainResult getOrderStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();
            statistics.put("total", ysOrderDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.eq("status", "0");
            statistics.put("pending", ysOrderDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.eq("status", "1");
            statistics.put("paid", ysOrderDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.eq("status", "2");
            statistics.put("shipped", ysOrderDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.eq("status", "3");
            statistics.put("completed", ysOrderDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.eq("status", "4");
            statistics.put("refund_pending", ysOrderDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.eq("status", "5");
            statistics.put("refunded", ysOrderDao.selectCount(wrapper));

            return DomainResult.success(statistics);
        } catch (Exception e) {
            return DomainResult.error("获取订单统计失败：" + e.getMessage());
        }
    }

    @Override
    public DomainResult exportOrders(String status) {
        try {
            QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();
            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", status);
            }
            List<YsOrder> list = ysOrderDao.selectList(wrapper);
            return DomainResult.success(list);
        } catch (Exception e) {
            return DomainResult.error("导出订单失败：" + e.getMessage());
        }
    }

    @Override
    public DomainResult getRefundList(Integer pageNum, Integer pageSize) {
        try {
            Page<YsOrder> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();
            wrapper.eq("status", "4");
            wrapper.orderByDesc("addtime");
            IPage<YsOrder> pageResult = ysOrderDao.selectPage(page, wrapper);

            List<YsOrder> orderList = pageResult.getRecords();
            for (YsOrder order : orderList) {
                YsUser user = ysUserDao.selectById(order.getUserId());
                if (user != null) {
                    order.setUsername(user.getUsername());
                }

                YsGoods goods = ysGoodsDao.selectById(order.getGoodsId());
                if (goods != null) {
                    order.setGoodsName(goods.getName());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", orderList);
            result.put("total", pageResult.getTotal());
            result.put("pageNum", pageResult.getCurrent());
            result.put("pageSize", pageResult.getSize());

            return DomainResult.success(result);
        } catch (Exception e) {
            return DomainResult.error("获取退款列表失败：" + e.getMessage());
        }
    }
}
