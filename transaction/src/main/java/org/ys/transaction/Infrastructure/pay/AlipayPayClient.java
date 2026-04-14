package org.ys.transaction.Infrastructure.pay;

import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AlipayPayClient implements ThirdPartyPayClient {
    private final PayProperties payProperties;

    public AlipayPayClient(PayProperties payProperties) {
        this.payProperties = payProperties;
    }

    @Override
    public String channel() {
        return "ALIPAY";
    }

    @Override
    public Map<String, Object> createRechargeOrder(String rechargeNo, BigDecimal amount, Long userId) {
        try {
            PayProperties.Alipay conf = payProperties.getAlipay();
            DefaultAlipayClient client = new DefaultAlipayClient(
                    conf.getGateway(),
                    conf.getAppId(),
                    conf.getMerchantPrivateKey(),
                    "json",
                    conf.getCharset(),
                    conf.getAlipayPublicKey(),
                    conf.getSignType()
            );
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(conf.getNotifyUrl());
            request.setReturnUrl(conf.getReturnUrl());

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(rechargeNo);
            model.setTotalAmount(amount.toPlainString());
            model.setSubject("用户余额充值");
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            request.setBizModel(model);

            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (!response.isSuccess()) {
                throw new IllegalStateException("支付宝下单失败: " + response.getSubMsg());
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("payContentType", "HTML_FORM");
            payload.put("payContent", response.getBody());
            return payload;
        } catch (Exception e) {
            throw new IllegalStateException("支付宝下单异常: " + e.getMessage(), e);
        }
    }
}
