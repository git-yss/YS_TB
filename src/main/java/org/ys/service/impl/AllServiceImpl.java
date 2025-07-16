package org.ys.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.ys.dao.YsUserDao;
import org.ys.entity.YsUser;
import org.ys.service.AllService;

import java.util.Map;

@Service
@Transactional
public class AllServiceImpl implements AllService {
    @Resource
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
}
