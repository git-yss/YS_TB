package org.ys.transaction.application.pay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.pay")
public class PayProperties {
    private Alipay alipay = new Alipay();
    private Wechat wechat = new Wechat();

    @Data
    public static class Alipay {
        private String appId;
        private String merchantPrivateKey;
        private String alipayPublicKey;
        private String gateway = "https://openapi.alipay.com/gateway.do";
        private String notifyUrl;
        private String returnUrl;
        private String signType = "RSA2";
        private String charset = "UTF-8";
    }

    @Data
    public static class Wechat {
        private String appId;
        private String mchId;
        private String privateKeyPath;
        private String merchantSerialNumber;
        private String apiV3Key;
        private String notifyUrl;
    }
}
