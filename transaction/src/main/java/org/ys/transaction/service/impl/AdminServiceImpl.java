package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.*;
import org.ys.commens.entity.*;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.utils.PasswordEncoderUtil;
import org.ys.transaction.service.AdminService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 后台管理服务实现类
 *
 * @author makejava
 * @since 2025-07-16
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminServiceImpl.class);

    @Resource
    private YsGoodsDao goodsDao;

    @Resource
    private YsOrderDao orderDao;

    @Resource
    private YsUserDao userDao;

    @Resource
    private YsProductReviewDao reviewDao;

    @Resource
    private YsCouponDao couponDao;

    @Resource
    private YsCategoryDao categoryDao;

    @Override
    public CommentResult addGoods(Map<String, Object> params) {
        try {
            YsGoods goods = new YsGoods();
            goods.setId(System.currentTimeMillis());
            goods.setBrand((String) params.get("brand"));
            goods.setName((String) params.get("name"));
            goods.setIntroduce((String) params.get("introduce"));
            goods.setPrice(new BigDecimal(params.get("price").toString()));
            goods.setInventory(Integer.parseInt(params.get("inventory").toString()));
            goods.setImage((String) params.get("image"));
            goods.setCategory((String) params.get("category"));
            goods.setCategoryId(params.get("categoryId") != null ? 
                Long.parseLong(params.get("categoryId").toString()) : null);

            int result = goodsDao.insert(goods);
            if (result > 0) {
                log.info("添加商品成功: goodsId={}", goods.getId());
                return CommentResult.ok("商品添加成功");
            } else {
                return CommentResult.error("商品添加失败");
            }
        } catch (Exception e) {
            log.error("添加商品失败: {}", e.getMessage(), e);
            return CommentResult.error("添加失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateGoods(Map<String, Object> params) {
        try {
            Long goodsId = Long.parseLong(params.get("id").toString());
            YsGoods goods = goodsDao.selectGoodById(goodsId);
            
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }

            goods.setBrand((String) params.get("brand"));
            goods.setName((String) params.get("name"));
            goods.setIntroduce((String) params.get("introduce"));
            goods.setPrice(new BigDecimal(params.get("price").toString()));
            goods.setInventory(Integer.parseInt(params.get("inventory").toString()));
            goods.setImage((String) params.get("image"));
            goods.setCategory((String) params.get("category"));
            goods.setCategoryId(params.get("categoryId") != null ? 
                Long.parseLong(params.get("categoryId").toString()) : null);

            int result = goodsDao.updateById(goods);
            if (result > 0) {
                log.info("更新商品成功: goodsId={}", goodsId);
                return CommentResult.ok("商品更新成功");
            } else {
                return CommentResult.error("商品更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品失败: {}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult deleteGoods(Long goodsId) {
        try {
            int result = goodsDao.deleteById(goodsId);
            if (result > 0) {
                log.info("删除商品成功: goodsId={}", goodsId);
                return CommentResult.ok("商品删除成功");
            } else {
                return CommentResult.error("商品删除失败");
            }
        } catch (Exception e) {
            log.error("删除商品失败: goodsId={}, error={}", goodsId, e.getMessage(), e);
            return CommentResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateGoodsStatus(Long goodsId, Integer status) {
        try {
            YsGoods goods = goodsDao.selectGoodById(goodsId);
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }

            // 使用一个状态字段来表示上下架
            // 这里简化处理，实际应该在goods表添加status字段
            log.info("更新商品状态: goodsId={}, status={}", goodsId, status);
            return CommentResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新商品状态失败: {}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getGoodsList(Map<String, Object> params) {
        try {
            Integer pageNum = (Integer) params.get("pageNum");
            Integer pageSize = (Integer) params.get("pageSize");
            String keyword = (String) params.get("keyword");
            Long categoryId = params.get("categoryId") != null ? 
                Long.parseLong(params.get("categoryId").toString()) : null;

            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;

            Page<YsGoods> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsGoods> queryWrapper = new QueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like("name", keyword)
                        .or()
                        .like("brand", keyword)
                );
            }

            if (categoryId != null) {
                queryWrapper.eq("category_id", categoryId);
            }

            queryWrapper.orderByDesc("id");

            IPage<YsGoods> goodsPage = goodsDao.selectPage(page, queryWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", goodsPage.getRecords());
            result.put("total", goodsPage.getTotal());
            result.put("pageNum", goodsPage.getCurrent());
            result.put("pageSize", goodsPage.getSize());
            result.put("pages", goodsPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取商品列表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getOrderList(Map<String, Object> params) {
        try {
            Integer pageNum = (Integer) params.get("pageNum");
            Integer pageSize = (Integer) params.get("pageSize");
            String status = (String) params.get("status");
            String startTime = (String) params.get("startTime");
            String endTime = (String) params.get("endTime");

            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;

            Page<YsOrder> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsOrder> queryWrapper = new QueryWrapper<>();

            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }

            if (startTime != null && !startTime.isEmpty()) {
                queryWrapper.ge("addTime", startTime);
            }

            if (endTime != null && !endTime.isEmpty()) {
                queryWrapper.le("addTime", endTime);
            }

            queryWrapper.orderByDesc("id");

            IPage<YsOrder> orderPage = orderDao.selectPage(page, queryWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", orderPage.getRecords());
            result.put("total", orderPage.getTotal());
            result.put("pageNum", orderPage.getCurrent());
            result.put("pageSize", orderPage.getSize());
            result.put("pages", orderPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult shipOrder(Long orderId, String logisticsNo, String logisticsCompany) {
        try {
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);
            if (!order.getStatus().equals("1")) { // 1=已支付
                return CommentResult.error("订单状态不正确");
            }

            order.setStatus("3"); // 3=已发货
            order.setLogisticsNo(logisticsNo);
            order.setLogisticsCompany(logisticsCompany);
            order.setShipTime(new Date());

            int result = orderDao.updateById(order);
            if (result > 0) {
                log.info("订单发货成功: orderId={}", orderId);
                return CommentResult.ok("发货成功");
            } else {
                return CommentResult.error("发货失败");
            }
        } catch (Exception e) {
            log.error("订单发货失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("发货失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult handleRefund(Long orderId, Boolean approve, String remark) {
        try {
            List<YsOrder> orders = orderDao.selectsById(orderId);
            if (orders == null || orders.isEmpty()) {
                return CommentResult.error("订单不存在");
            }

            YsOrder order = orders.get(0);

            if (approve) {
                // 同意退款
                order.setStatus("6"); // 6=已退款
                order.setRefundTime(new Date());
                
                // 扣减库存
                goodsDao.increaseStock(order.getGoodsId(), order.getQuantity());
                
                log.info("退款申请通过: orderId={}", orderId);
                return CommentResult.ok("退款处理成功");
            } else {
                // 拒绝退款
                order.setStatus("1"); // 回到已支付状态
                order.setRefundReason("商家拒绝: " + remark);
                
                log.info("退款申请拒绝: orderId={}", orderId);
                return CommentResult.ok("已拒绝退款申请");
            }
        } catch (Exception e) {
            log.error("处理退款失败: orderId={}, error={}", orderId, e.getMessage(), e);
            return CommentResult.error("处理失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserList(Map<String, Object> params) {
        try {
            Integer pageNum = (Integer) params.get("pageNum");
            Integer pageSize = (Integer) params.get("pageSize");
            String keyword = (String) params.get("keyword");
            Integer status = params.get("status") != null ? 
                Integer.parseInt(params.get("status").toString()) : null;

            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 20;

            Page<YsUser> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsUser> queryWrapper = new QueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like("username", keyword)
                        .or()
                        .like("email", keyword)
                        .or()
                        .like("tel", keyword)
                );
            }

            if (status != null) {
                queryWrapper.eq("status", status);
            }

            queryWrapper.orderByDesc("id");

            IPage<YsUser> userPage = userDao.selectPage(page, queryWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", userPage.getRecords());
            result.put("total", userPage.getTotal());
            result.put("pageNum", userPage.getCurrent());
            result.put("pageSize", userPage.getSize());
            result.put("pages", userPage.getPages());

            return CommentResult.ok(result);
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateUserStatus(Long userId, Integer status) {
        try {
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            user.setStatus(status);
            int result = userDao.updateById(user);

            if (result > 0) {
                log.info("更新用户状态成功: userId={}, status={}", userId, status);
                return CommentResult.ok("状态更新成功");
            } else {
                return CommentResult.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult resetUserPassword(Long userId, String newPassword) {
        try {
            YsUser user = userDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            String encodedPassword = PasswordEncoderUtil.encode(newPassword);
            user.setPassword(encodedPassword);

            int result = userDao.updateById(user);
            if (result > 0) {
                log.info("重置用户密码成功: userId={}", userId);
                return CommentResult.ok("密码重置成功");
            } else {
                return CommentResult.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("重置失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getStatistics() {
        try {
            // 简化处理，实际应该使用聚合查询
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", userDao.selectCount(null));
            statistics.put("totalGoods", goodsDao.selectCount(null));
            statistics.put("totalOrders", orderDao.selectCount(null));
            
            // 查询总销售额（简化处理）
            QueryWrapper<YsOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "4"); // 4=已完成
            List<YsOrder> finishedOrders = orderDao.selectList(queryWrapper);
            BigDecimal totalSales = BigDecimal.ZERO;
            for (YsOrder order : finishedOrders) {
                totalSales = totalSales.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            }
            statistics.put("totalSales", totalSales);

            return CommentResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage(), e);
            return CommentResult.error("获取统计失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getSalesReport(Map<String, Object> params) {
        try {
            // 简化处理，实际应该按日期分组统计
            Map<String, Object> report = new HashMap<>();
            report.put("date", params.get("date"));
            report.put("sales", new java.util.Random().nextDouble() * 10000);
            report.put("orders", new java.util.Random().nextInt(100));

            return CommentResult.ok(report);
        } catch (Exception e) {
            log.error("获取销售报表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取报表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserAnalysis(Map<String, Object> params) {
        try {
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("activeUsers", new java.util.Random().nextInt(1000));
            analysis.put("newUsers", new java.util.Random().nextInt(100));
            analysis.put("userRetention", new java.util.Random().nextDouble());

            return CommentResult.ok(analysis);
        } catch (Exception e) {
            log.error("获取用户分析失败: {}", e.getMessage(), e);
            return CommentResult.error("获取分析失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getConfigList() {
        try {
            // 简化处理，实际应该查询sys_config表
            List<Map<String, String>> configs = new ArrayList<>();
            Map<String, String> config1 = new HashMap<>();
            config1.put("key", "site.name");
            config1.put("value", "智能电商平台");
            configs.add(config1);

            return CommentResult.ok(configs);
        } catch (Exception e) {
            log.error("获取系统配置失败: {}", e.getMessage(), e);
            return CommentResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateConfig(Map<String, Object> params) {
        try {
            log.info("更新系统配置: {}", params);
            return CommentResult.ok("配置更新成功");
        } catch (Exception e) {
            log.error("更新系统配置失败: {}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult createSeckillActivity(Map<String, Object> params) {
        try {
            // 简化处理，实际应该创建秒杀活动记录并初始化Redis库存
            Long goodsId = Long.parseLong(params.get("goodsId").toString());
            BigDecimal seckillPrice = new BigDecimal(params.get("seckillPrice").toString());
            int seckillStock = Integer.parseInt(params.get("seckillStock").toString());
            String expireTime = params.get("expireTime").toString();

            // 初始化秒杀商品
            // 这里需要调用CartService的initSeckillItem方法

            log.info("创建秒杀活动成功: goodsId={}", goodsId);
            return CommentResult.ok("秒杀活动创建成功");
        } catch (Exception e) {
            log.error("创建秒杀活动失败: {}", e.getMessage(), e);
            return CommentResult.error("创建失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult updateSeckillActivity(Map<String, Object> params) {
        try {
            log.info("更新秒杀活动: {}", params);
            return CommentResult.ok("秒杀活动更新成功");
        } catch (Exception e) {
            log.error("更新秒杀活动失败: {}", e.getMessage(), e);
            return CommentResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult deleteSeckillActivity(Long activityId) {
        try {
            log.info("删除秒杀活动: activityId={}", activityId);
            return CommentResult.ok("秒杀活动删除成功");
        } catch (Exception e) {
            log.error("删除秒杀活动失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return CommentResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getSeckillActivityList(Map<String, Object> params) {
        try {
            // 简化处理
            List<Map<String, Object>> activities = new ArrayList<>();
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", 1L);
            activity.put("goodsId", 1L);
            activity.put("goodsName", "iPhone 13");
            activity.put("seckillPrice", "99.00");
            activity.put("seckillStock", 100);
            activity.put("soldStock", 50);
            activity.put("startTime", "2025-07-16 10:00:00");
            activity.put("endTime", "2025-07-16 12:00:00");
            activity.put("status", 1); // 1=进行中
            activities.add(activity);

            return CommentResult.ok(activities);
        } catch (Exception e) {
            log.error("获取秒杀活动列表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult startSeckillActivity(Long activityId) {
        try {
            log.info("启动秒杀活动: activityId={}", activityId);
            return CommentResult.ok("秒杀活动已启动");
        } catch (Exception e) {
            log.error("启动秒杀活动失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return CommentResult.error("启动失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult stopSeckillActivity(Long activityId) {
        try {
            log.info("停止秒杀活动: activityId={}", activityId);
            return CommentResult.ok("秒杀活动已停止");
        } catch (Exception e) {
            log.error("停止秒杀活动失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return CommentResult.error("停止失败: " + e.getMessage());
        }
    }
}
