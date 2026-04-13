package org.ys.transaction.application.pay;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class WechatPayClient implements ThirdPartyPayClient {
    private final PayProperties payProperties;

    public WechatPayClient(PayProperties payProperties) {
        this.payProperties = payProperties;
    }

    @Override
    public String channel() {
        return "WECHAT";
    }

    @Override
    public Map<String, Object> createRechargeOrder(String rechargeNo, BigDecimal amount, Long userId) {
        try {
            PayProperties.Wechat conf = payProperties.getWechat();
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(conf.getMchId())
                    .privateKeyFromPath(conf.getPrivateKeyPath())
                    .merchantSerialNumber(conf.getMerchantSerialNumber())
                    .apiV3Key(conf.getApiV3Key())
                    .build();
            NativePayService service = new NativePayService.Builder()
                    .config(config)
                    .build();

            PrepayRequest request = new PrepayRequest();
            request.setAppid(conf.getAppId());
            request.setMchid(conf.getMchId());
            request.setDescription("用户余额充值");
            request.setOutTradeNo(rechargeNo);
            request.setNotifyUrl(conf.getNotifyUrl());

            Amount reqAmount = new Amount();
            reqAmount.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(reqAmount);

            PrepayResponse response = service.prepay(request);

            Map<String, Object> payload = new HashMap<>();
            payload.put("payContentType", "QRCODE_URL");
            payload.put("payContent", response.getCodeUrl());
            return payload;
        } catch (Exception e) {
            throw new IllegalStateException("微信下单异常: " + e.getMessage(), e);
        }
    }
}
