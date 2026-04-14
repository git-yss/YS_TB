package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.*;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.RechargeApplicationService;
import org.ys.transaction.application.UserApplicationService;
import org.ys.transaction.Infrastructure.pay.PayProperties;
import org.ys.transaction.domain.aggregate.UserAggregate;
import com.alipay.api.internal.util.AlipaySignature;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.model.Transaction;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserApplicationService userApplicationService;
    @Resource
    private RechargeApplicationService rechargeApplicationService;
    @Resource
    private PayProperties payProperties;

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

    @PostMapping("recharge/create")
    @ResponseBody
    public CommentResult createRechargeOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            BigDecimal amount = new BigDecimal(params.get("amount").toString());
            String channel = params.get("channel").toString();
            return CommentResult.success(rechargeApplicationService.createRechargeOrder(userId, amount, channel));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("recharge/callback")
    @ResponseBody
    public CommentResult rechargeCallback(@RequestBody Map<String, Object> params) {
        try {
            String rechargeNo = params.get("rechargeNo").toString();
            String channel = params.get("channel").toString();
            String tradeNo = params.get("tradeNo") != null ? params.get("tradeNo").toString() : "";
            return CommentResult.success(rechargeApplicationService.confirmRechargeSuccess(rechargeNo, channel, tradeNo));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("recharge/status")
    @ResponseBody
    public CommentResult rechargeStatus(@RequestBody Map<String, Object> params) {
        try {
            String rechargeNo = params.get("rechargeNo").toString();
            return CommentResult.success(rechargeApplicationService.getRechargeStatus(rechargeNo));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @PostMapping("recharge/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String[] values = entry.getValue();
                if (values != null && values.length > 0) {
                    params.put(entry.getKey(), values[0]);
                }
            }
            boolean signOk = AlipaySignature.rsaCheckV1(
                    params,
                    payProperties.getAlipay().getAlipayPublicKey(),
                    payProperties.getAlipay().getCharset(),
                    payProperties.getAlipay().getSignType()
            );
            if (!signOk) {
                return "failure";
            }
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            rechargeApplicationService.confirmRechargeSuccess(outTradeNo, "ALIPAY", tradeNo);
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @PostMapping("recharge/wechat/notify")
    @ResponseBody
    public Map<String, String> wechatNotify(HttpServletRequest request, @RequestBody String body) {
        Map<String, String> result = new HashMap<>();
        try {
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(payProperties.getWechat().getMchId())
                    .privateKeyFromPath(payProperties.getWechat().getPrivateKeyPath())
                    .merchantSerialNumber(payProperties.getWechat().getMerchantSerialNumber())
                    .apiV3Key(payProperties.getWechat().getApiV3Key())
                    .build();
            NotificationConfig notificationConfig = (NotificationConfig) config;
            NotificationParser parser = new NotificationParser(notificationConfig);
            com.wechat.pay.java.core.notification.RequestParam param = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .signType(request.getHeader("Wechatpay-Signature-Type"))
                    .body(body)
                    .build();
            Transaction transaction = parser.parse(param, Transaction.class);
            String rechargeNo = transaction.getOutTradeNo();
            String tradeNo = transaction.getTransactionId();
            rechargeApplicationService.confirmRechargeSuccess(rechargeNo, "WECHAT", tradeNo);
            result.put("code", "SUCCESS");
            result.put("message", "成功");
            return result;
        } catch (Exception e) {
            result.put("code", "FAIL");
            result.put("message", e.getMessage());
            return result;
        }
    }
}

