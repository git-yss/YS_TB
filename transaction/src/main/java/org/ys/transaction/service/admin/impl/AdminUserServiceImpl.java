package org.ys.transaction.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.dao.YsUserDao;
import org.ys.commens.entity.YsOrder;
import org.ys.commens.entity.YsUser;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.admin.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理服务实现
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private YsUserDao ysUserDao;

    @Resource
    private YsOrderDao ysOrderDao;

    @Override
    public CommentResult adminLogin(String username, String password) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            Map<String, Object> result = new HashMap<>();
            result.put("username", "admin");
            result.put("role", "admin");
            result.put("token", generateToken());
            return CommentResult.success(result);
        }
        return CommentResult.error("用户名或密码错误");
    }

    private String generateToken() {
        return "admin_token_" + System.currentTimeMillis();
    }

    @Override
    public CommentResult getUserList(String keyword, String status, Integer pageNum, Integer pageSize) {
        try {
            Page<YsUser> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsUser> wrapper = new QueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like("username", keyword)
                        .or()
                        .like("email", keyword)
                        .or()
                        .like("tel", keyword);
            }

            if (status != null && !status.isEmpty()) {
                if ("1".equals(status)) {
                    wrapper.isNotNull("status");
                } else if ("0".equals(status)) {
                    wrapper.isNull("status");
                }
            }

            wrapper.orderByDesc("id");
            IPage<YsUser> pageResult = ysUserDao.selectPage(page, wrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("pageNum", pageResult.getCurrent());
            result.put("pageSize", pageResult.getSize());

            return CommentResult.success(result);
        } catch (Exception e) {
            return CommentResult.error("获取用户列表失败：" + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserDetail(Long id) {
        try {
            YsUser user = ysUserDao.selectById(id);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            QueryWrapper<YsOrder> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("user_id", id);
            int orderCount = (int) ysOrderDao.selectCount(orderWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("orderCount", orderCount);

            return CommentResult.success(result);
        } catch (Exception e) {
            return CommentResult.error("获取用户详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult banUser(Long id, String reason) {
        try {
            YsUser user = ysUserDao.selectById(id);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            YsUser updateUser = new YsUser();
            updateUser.setId(id);
            updateUser.setStatus("0");

            int count = ysUserDao.updateById(updateUser);
            if (count > 0) {
                return CommentResult.success("封禁用户成功");
            }
            return CommentResult.error("封禁用户失败");
        } catch (Exception e) {
            return CommentResult.error("封禁用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult unbanUser(Long id) {
        try {
            YsUser user = ysUserDao.selectById(id);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            YsUser updateUser = new YsUser();
            updateUser.setId(id);
            updateUser.setStatus("1");

            int count = ysUserDao.updateById(updateUser);
            if (count > 0) {
                return CommentResult.success("解封用户成功");
            }
            return CommentResult.error("解封用户失败");
        } catch (Exception e) {
            return CommentResult.error("解封用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult updateBalance(Long userId, Double amount, String operation, String remark) {
        try {
            YsUser user = ysUserDao.selectById(userId);
            if (user == null) {
                return CommentResult.error("用户不存在");
            }

            BigDecimal newBalance;
            if ("add".equals(operation)) {
                newBalance = user.getBalance().add(BigDecimal.valueOf(amount));
            } else if ("subtract".equals(operation)) {
                newBalance = user.getBalance().subtract(BigDecimal.valueOf(amount));
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    return CommentResult.error("余额不足");
                }
            } else {
                return CommentResult.error("操作类型错误");
            }

            YsUser updateUser = new YsUser();
            updateUser.setId(userId);
            updateUser.setBalance(newBalance);

            int count = ysUserDao.updateById(updateUser);
            if (count > 0) {
                return CommentResult.success("更新余额成功");
            }
            return CommentResult.error("更新余额失败");
        } catch (Exception e) {
            return CommentResult.error("更新余额失败：" + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserOrders(Long id, Integer pageNum, Integer pageSize) {
        try {
            Page<YsOrder> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsOrder> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", id);
            wrapper.orderByDesc("addtime");
            IPage<YsOrder> pageResult = ysOrderDao.selectPage(page, wrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("pageNum", pageResult.getCurrent());
            result.put("pageSize", pageResult.getSize());

            return CommentResult.success(result);
        } catch (Exception e) {
            return CommentResult.error("获取用户订单失败：" + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            QueryWrapper<YsUser> wrapper = new QueryWrapper<>();
            statistics.put("total", ysUserDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.isNull("status");
            statistics.put("banned", ysUserDao.selectCount(wrapper));

            wrapper = new QueryWrapper<>();
            wrapper.isNotNull("status");
            statistics.put("active", ysUserDao.selectCount(wrapper));

            return CommentResult.success(statistics);
        } catch (Exception e) {
            return CommentResult.error("获取用户统计失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult batchBanUsers(List<Long> userIds) {
        try {
            int successCount = 0;
            for (Long userId : userIds) {
                CommentResult result = banUser(userId, "批量封禁");
                if (result.isSuccess()) {
                    successCount++;
                }
            }
            return CommentResult.success("批量封禁成功，共封禁 " + successCount + " 个用户");
        } catch (Exception e) {
            return CommentResult.error("批量封禁失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult batchUnbanUsers(List<Long> userIds) {
        try {
            int successCount = 0;
            for (Long userId : userIds) {
                CommentResult result = unbanUser(userId);
                if (result.isSuccess()) {
                    successCount++;
                }
            }
            return CommentResult.success("批量解封成功，共解封 " + successCount + " 个用户");
        } catch (Exception e) {
            return CommentResult.error("批量解封失败：" + e.getMessage());
        }
    }
}
