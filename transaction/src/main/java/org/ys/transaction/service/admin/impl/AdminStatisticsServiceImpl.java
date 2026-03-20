package org.ys.transaction.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.admin.AdminStatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台统计数据服务实现
 */
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    @Resource
    private YsUserDao ysUserDao;

    @Resource
    private YsGoodsDao ysGoodsDao;

    @Resource
    private YsOrderDao ysOrderDao;

    @Override
    public CommentResult getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();
            stats.put("totalOrders", ysOrderDao.selectCount(wrapper));

            QueryWrapper<YsOrder> paidWrapper = new QueryWrapper<>();
            paidWrapper.in("status", "1", "2", "3");
            stats.put("paidOrders", ysOrderDao.selectCount(paidWrapper));

            QueryWrapper<YsOrder> pendingWrapper = new QueryWrapper<>();
            pendingWrapper.eq("status", "0");
            stats.put("pendingOrders", ysOrderDao.selectCount(pendingWrapper));

            QueryWrapper<YsOrder> shippedWrapper = new QueryWrapper<>();
            shippedWrapper.eq("status", "2");
            stats.put("shippedOrders", ysOrderDao.selectCount(shippedWrapper));

            stats.put("totalUsers", ysUserDao.selectCount(new QueryWrapper<>()));

            stats.put("totalGoods", ysGoodsDao.selectCount(new QueryWrapper<>()));

            QueryWrapper<YsOrder> revenueWrapper = new QueryWrapper<>();
            revenueWrapper.in("status", "1", "2", "3");
            List<YsOrder> paidOrders = ysOrderDao.selectList(revenueWrapper);
            BigDecimal totalRevenue = paidOrders.stream()
                    .map(YsOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);

            stats.put("newOrdersToday", getTodayOrdersCount());

            stats.put("newUsersToday", getTodayUsersCount());

            return CommentResult.success(stats);
        } catch (Exception e) {
            return CommentResult.error("获取统计数据失败：" + e.getMessage());
        }
    }

    private Long getTodayOrdersCount() {
        QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        wrapper.ge("addtime", startOfDay);
        return Long.valueOf(ysOrderDao.selectCount(wrapper));
    }

    private Long getTodayUsersCount() {
        return 0L;
    }
}
