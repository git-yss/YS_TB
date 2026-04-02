package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.*;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.UserApplicationService;
import org.ys.transaction.domain.aggregate.UserAggregate;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserApplicationService userApplicationService;

    @PostMapping("register")
    @ResponseBody
    public CommentResult register(@RequestBody Map<String, Object> params) {
        try {
            userApplicationService.register(params);
            return CommentResult.success("注册成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
    @PostMapping("login")
    @ResponseBody
    public CommentResult login(@RequestBody Map<String, Object> params) {
        try {
            userApplicationService.login(params);
            return CommentResult.success("登录成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @GetMapping("info")
    @ResponseBody
    public CommentResult info(@RequestParam("userId") Long userId) {
        try {
            UserAggregate userInfo = userApplicationService.getUserInfo(userId);
            return CommentResult.success(userInfo);
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("changePassword")
    @ResponseBody
    public CommentResult changePassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String oldPassword = params.get("oldPassword") != null ? params.get("oldPassword").toString() : "";
        String newPassword = params.get("newPassword") != null ? params.get("newPassword").toString() : "";
        try {
            userApplicationService.changePassword(userId, oldPassword, newPassword);
            return CommentResult.success("密码修改成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("update")
    @ResponseBody
    public CommentResult update(@RequestBody Map<String, Object> params) {
        try {
            userApplicationService.updateUserInfo(params);
            return CommentResult.success("信息更新成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}
