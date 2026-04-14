package org.ys.transaction.Infrastructure.pay;

import java.math.BigDecimal;
import java.util.Map;

public interface ThirdPartyPayClient {
    String channel();

    Map<String, Object> createRechargeOrder(String rechargeNo, BigDecimal amount, Long userId);
}
