package org.ys.transaction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.UserService;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("register")
    @ResponseBody
    public CommentResult register(@RequestBody Map<String, Object> params) {
        return userService.register(params);
    }

    @GetMapping("info")
    @ResponseBody
    public CommentResult info(@RequestParam("userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    @PostMapping("changePassword")
    @ResponseBody
    public CommentResult changePassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String oldPassword = params.get("oldPassword") != null ? params.get("oldPassword").toString() : "";
        String newPassword = params.get("newPassword") != null ? params.get("newPassword").toString() : "";
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @PostMapping("update")
    @ResponseBody
    public CommentResult update(@RequestBody Map<String, Object> params) {
        return userService.updateUserInfo(params);
    }
}
