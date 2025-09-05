package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsUser;
import org.ys.commens.dao.YsUserDao;
import org.ys.transaction.service.IndexService;


import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AllServiceImpl implements IndexService {
    @Autowired
    private YsUserDao ysUserDao;

    @Autowired
    private YsGoodsDao ysGoodsDao;

    @Override
    public void login(Map<String, Object> map) {
        String username = map.get("username") != null ? map.get("username").toString() : "";
        String password = map.get("password") != null ? map.get("password").toString() : "";

        YsUser user = ysUserDao.queryUser(username, password);
        if (ObjectUtils.isEmpty(user)) {
            throw new RuntimeException("账号密码错误");
        }
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
