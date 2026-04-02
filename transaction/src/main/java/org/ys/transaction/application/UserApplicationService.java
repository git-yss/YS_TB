package org.ys.transaction.application;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.application.conver.UserConver;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.respository.YsUserRespository;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class UserApplicationService {

    @Resource
    private YsUserRespository ysUserRespository;

    @Transactional
    public void register(Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String email = (String) params.get("email");
        String tel = (String) params.get("tel");

        UserAggregate existUser = ysUserRespository.selectByName(username);
        existUser.checkNameDump();
        UserAggregate existByEmail = ysUserRespository.selectByEmail(new UserAggregate(
                YsUser.rehydrate(null, null, null, null, null, null, email, null, null, null), null
        ));
        existByEmail.checkEmailDump(email);

        YsUser user = YsUser.rehydrate(
                System.currentTimeMillis(), username, password, null, null,
                java.math.BigDecimal.ZERO, email, tel, String.valueOf(1), null
        );
        UserAggregate userAggregate = UserConver.INSTANCE.voToAggregate(user, null);
        userAggregate.checkBaseInfo();
        ysUserRespository.insert(userAggregate);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(Map<String, Object> params) {
        UserAggregate userAggregate = ysUserRespository.selectByName(params.get("username").toString());
        userAggregate.checkPassword(params.get("password").toString());
        return (Map<String, Object>) userAggregate.getUser();
    }
    @Transactional(readOnly = true)
    public UserAggregate getUserInfo(Long userId) {
        return ysUserRespository.selectAggregateById(String.valueOf(userId));
    }

    @Transactional
    public void updateUserInfo(Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String age = (String) params.get("age");
        String sex = (String) params.get("sex");
        UserAggregate userAggregate = ysUserRespository.selectAggregateById(String.valueOf(userId));
        userAggregate.isEmp();
        YsUser user = userAggregate.getUser();
        YsUser updatedUser = YsUser.rehydrate(
                user.getId(), user.getUsername(), user.getPassword(), age != null ? age : user.getAge(),
                sex != null ? sex : user.getSex(), user.getBalance(), user.getEmail(), user.getTel(),
                user.getStatus(), user.getCreateTime()
        );
        ysUserRespository.updateById(new UserAggregate(updatedUser, null));

    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserAggregate userAggregate = ysUserRespository.selectAggregateById(String.valueOf(userId));
        userAggregate.isEmp();
        userAggregate.checkPassword(oldPassword);
        userAggregate.checkPwsInfo(newPassword);
        YsUser user = userAggregate.getUser();
        user = YsUser.rehydrate(
                user.getId(), user.getUsername(), newPassword, user.getAge(), user.getSex(),
                user.getBalance(), user.getEmail(), user.getTel(), user.getStatus(), user.getCreateTime()
        );
        ysUserRespository.updateById(new UserAggregate(user, null));

    }
}
