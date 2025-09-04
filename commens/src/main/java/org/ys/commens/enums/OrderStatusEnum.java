package org.ys.commens.enums;

public enum OrderStatusEnum {
    ORDER_CREATING(1, "生成订单中"),
    PENDING_PAYMENT(2, "待支付"),
    PAID(3, "已支付"),
    CANCELLED(4, "已取消"),
    EXPIRECANCELLED(5, "过期取消");

    private final int code;
    private final String description;

    OrderStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取订单状态枚举
     * @param code 状态码
     * @return 对应的订单状态枚举
     */
    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }

        throw new IllegalArgumentException("未知的订单状态码: " + code);
    }

    @Override
    public String toString() {
        return "OrderStatusEnum{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }

}
