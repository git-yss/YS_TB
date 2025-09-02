package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsUser;
import org.ys.commens.dao.YsUserDao;
import org.ys.transaction.service.IndexService;


import java.util.Map;

@Service
@Transactional
public class AllServiceImpl implements IndexService {
    @Autowired
    private YsUserDao ysUserDao;
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
        long current = Long.parseLong(map.get("current").toString());
        long size = Long.parseLong(map.get("size").toString());

        // 创建分页对象
        Page<YsGoods> page = new Page<>(current, size);
        page.setTotal(ysUserDao.queryAllGoodsPage(page,map).size());
        page.setRecords(ysUserDao.queryAllGoodsPage(page,map));
        return page;
    }
}
