package org.ys.transaction.application;

import org.ys.transaction.Infrastructure.pojo.YsUser;

import java.util.HashMap;
import java.util.Map;

public class UserApplicationService {

    public void register(Map<String, Object> params) {

    }

    public Object getUserInfo(Long userId) {
       return new HashMap<String, Object>() {{
           put("id", userId);
           put("username", "ys");
           put("age", 18);
           put("sex", "男");
           put("balance", 100);
           put("email", "ys@163.com");
           put("tel", "12345678901");
           put("status", "1");
       }};
    }

    public void updateUserInfo(Map<String, Object> params) {
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
    }
}
