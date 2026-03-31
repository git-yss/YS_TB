package org.ys.transaction.domain.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.ys.transaction.domain.inteface.IndexService;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.pojo.YsGoods;
import org.ys.transaction.Infrastructure.pojo.YsUser;
import org.ys.transaction.Infrastructure.utils.PasswordEncoderUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AllServiceImpl implements IndexService {
    @Autowired
    private YsUserDao ysUserDao;

    @Autowired
    private YsGoodsDao ysGoodsDao;

    @Override
    public Map<String, Object> login(Map<String, Object> map) {
        String username = map.get("username") != null ? map.get("username").toString().trim() : "";
        String password = map.get("password") != null ? map.get("password").toString() : "";

        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        YsUser user = ysUserDao.selectByName(username);
        if (ObjectUtils.isEmpty(user) || !PasswordEncoderUtil.matches(password, user.getPassword())) {
            throw new IllegalStateException("账号或密码错误");
        }
        if (!"1".equals(user.getStatus())) {
            throw new IllegalStateException("账号已封禁");
        }

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (user.getId() + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("tel", user.getTel());
        userInfo.put("balance", user.getBalance());
        userInfo.put("status", user.getStatus());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);

        return data;
    }

    @Override
    public Page<YsGoods> queryAllGoods(Map<String, Object> map) {
        // 从 map 中获取当前页码和每页大小
        // 安全地获取分页参数
        long current = map.get("current") != null ?
                Long.parseLong(map.get("current").toString()) : 1L;
        long size = map.get("size") != null ?
                Long.parseLong(map.get("size").toString()) : 10L;
        // 创建分页对象
        IPage<YsGoods> page = new Page<>(current, size);
        // 直接使用MyBatis-Plus的分页查询，不需要手动处理偏移量
        return ysGoodsDao.queryAllGoodsPage(page, map);
    }
}
