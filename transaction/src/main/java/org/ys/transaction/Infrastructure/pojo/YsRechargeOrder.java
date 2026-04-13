package org.ys.transaction.Infrastructure.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class YsRechargeOrder {
    private Long id;
    private String rechargeNo;
    private Long userId;
    private String channel;
    private BigDecimal amount;
    private String status;
    private String tradeNo;
    private String payContentType;
    private String payContent;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
}
