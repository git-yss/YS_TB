package org.ys.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.ys.commens.entity.YsUser;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.dao.YsUserDao;
import org.ys.transaction.service.AllService;


import java.util.Map;

@Service
@Transactional
public class AllServiceImpl implements AllService {
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
}
