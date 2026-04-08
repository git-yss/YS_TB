package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.*;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.UserApplicationService;
import org.ys.transaction.domain.aggregate.UserAggregate;

import jakarta.annotation.Resource;
import java.util.HashMap;
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
            return CommentResult.success("娉ㄥ唽鎴愬姛");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
    @PostMapping("login")
    @ResponseBody
    public CommentResult login(@RequestBody Map<String, Object> params) {
        try {
            Object userInfo = userApplicationService.login(params);
            Map<String, Object> payload = new HashMap<>();
            payload.put("userInfo", userInfo);
            // 褰撳墠椤圭洰鏈惎鐢?JWT锛岃繑鍥炲彲鐢ㄥ崰浣?token 浠ラ€氳繃鍓嶇閴存潈瀹堝崼
            payload.put("token", "session-token");
            return CommentResult.success(payload);
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
            return CommentResult.success("瀵嗙爜淇敼鎴愬姛");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("update")
    @ResponseBody
    public CommentResult update(@RequestBody Map<String, Object> params) {
        try {
            userApplicationService.updateUserInfo(params);
            return CommentResult.success("淇℃伅鏇存柊鎴愬姛");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}

