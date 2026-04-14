package org.ys.transaction.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.Infrastructure.dao.YsRechargeOrderDao;
import org.ys.transaction.Infrastructure.pojo.YsRechargeOrder;
import org.ys.transaction.Infrastructure.pay.ThirdPartyPayClient;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.respository.YsUserRespository;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RechargeApplicationService {

    private static final BigDecimal MAX_RECHARGE_AMOUNT = new BigDecimal("50000");

    @Resource
    private YsUserRespository ysUserRespository;

    @Resource
    private List<ThirdPartyPayClient> payClients;
    @Resource
    private YsRechargeOrderDao ysRechargeOrderDao;

    @Transactional
    public Map<String, Object> createRechargeOrder(Long userId, BigDecimal amount, String channel) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        if (amount.compareTo(MAX_RECHARGE_AMOUNT) > 0) {
            throw new IllegalArgumentException("单次充值不能超过50000元");
        }

        String normalizedChannel = normalizeChannel(channel);
        ThirdPartyPayClient payClient = findPayClient(normalizedChannel);
        UserAggregate userAggregate = ysUserRespository.selectAggregateById(String.valueOf(userId));
        if (userAggregate == null || userAggregate.getUser() == null) {
            throw new IllegalStateException("用户不存在");
        }

        String rechargeNo = "RC" + System.currentTimeMillis() + userId;
        Map<String, Object> payPayload = payClient.createRechargeOrder(rechargeNo, amount, userId);

        YsRechargeOrder rechargeOrder = new YsRechargeOrder();
        rechargeOrder.setRechargeNo(rechargeNo);
        rechargeOrder.setUserId(userId);
        rechargeOrder.setAmount(amount);
        rechargeOrder.setChannel(normalizedChannel);
        rechargeOrder.setStatus("PENDING");
        rechargeOrder.setPayContentType(stringValue(payPayload.get("payContentType")));
        rechargeOrder.setPayContent(stringValue(payPayload.get("payContent")));
        ysRechargeOrderDao.insert(rechargeOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("rechargeNo", rechargeNo);
        result.put("channel", normalizedChannel);
        result.put("amount", amount);
        result.putAll(payPayload);
        return result;
    }

    @Transactional
    public Map<String, Object> confirmRechargeSuccess(String rechargeNo, String channel, String tradeNo) {
        if (rechargeNo == null || rechargeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("充值单号不能为空");
        }
        YsRechargeOrder order = ysRechargeOrderDao.selectByRechargeNo(rechargeNo);
        if (order == null) {
            throw new IllegalStateException("充值单不存在或已过期");
        }
        String normalizedChannel = normalizeChannel(channel);
        if (!normalizedChannel.equals(order.getChannel())) {
            throw new IllegalStateException("支付渠道不匹配");
        }
        if ("SUCCESS".equals(order.getStatus())) {
            return buildBalanceResult(order.getUserId(), order.getAmount(), rechargeNo, normalizedChannel, tradeNo, true);
        }

        UserAggregate userAggregate = ysUserRespository.selectAggregateById(String.valueOf(order.getUserId()));
        if (userAggregate == null || userAggregate.getUser() == null) {
            throw new IllegalStateException("用户不存在");
        }
        YsUser user = userAggregate.getUser();
        BigDecimal oldBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = oldBalance.add(order.getAmount());
        YsUser updatedUser = YsUser.rehydrate(
                user.getId(), user.getUsername(), user.getPassword(), user.getAge(), user.getSex(),
                newBalance, user.getEmail(), user.getTel(), user.getStatus(), user.getCreateTime()
        );
        ysUserRespository.updateBalanceById(new UserAggregate(updatedUser, null));

        ysRechargeOrderDao.markSuccess(rechargeNo, tradeNo);
        return buildBalanceResult(order.getUserId(), order.getAmount(), rechargeNo, normalizedChannel, tradeNo, false);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRechargeStatus(String rechargeNo) {
        if (rechargeNo == null || rechargeNo.trim().isEmpty()) {
            throw new IllegalArgumentException("充值单号不能为空");
        }
        YsRechargeOrder order = ysRechargeOrderDao.selectByRechargeNo(rechargeNo);
        if (order == null) {
            throw new IllegalStateException("充值单不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rechargeNo", order.getRechargeNo());
        result.put("status", order.getStatus());
        result.put("channel", order.getChannel());
        result.put("tradeNo", order.getTradeNo());
        result.put("amount", order.getAmount());
        if ("SUCCESS".equals(order.getStatus())) {
            UserAggregate latest = ysUserRespository.selectAggregateById(String.valueOf(order.getUserId()));
            BigDecimal balance = latest != null && latest.getUser() != null && latest.getUser().getBalance() != null
                    ? latest.getUser().getBalance()
                    : BigDecimal.ZERO;
            result.put("balance", balance);
        }
        return result;
    }

    private Map<String, Object> buildBalanceResult(Long userId,
                                                   BigDecimal amount,
                                                   String rechargeNo,
                                                   String channel,
                                                   String tradeNo,
                                                   boolean repeated) {
        UserAggregate latest = ysUserRespository.selectAggregateById(String.valueOf(userId));
        BigDecimal balance = latest != null && latest.getUser() != null && latest.getUser().getBalance() != null
                ? latest.getUser().getBalance()
                : BigDecimal.ZERO;
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("rechargeNo", rechargeNo);
        result.put("channel", channel);
        result.put("tradeNo", tradeNo);
        result.put("rechargeAmount", amount);
        result.put("balance", balance);
        result.put("repeated", repeated);
        return result;
    }

    private String normalizeChannel(String channel) {
        if (channel == null) {
            throw new IllegalArgumentException("支付渠道不能为空");
        }
        String c = channel.trim().toUpperCase();
        if ("ALIPAY".equals(c) || "WECHAT".equals(c)) {
            return c;
        }
        throw new IllegalArgumentException("仅支持 ALIPAY 或 WECHAT");
    }

    private ThirdPartyPayClient findPayClient(String channel) {
        for (ThirdPartyPayClient client : payClients) {
            if (channel.equals(client.channel())) {
                return client;
            }
        }
        throw new IllegalStateException("未找到支付客户端: " + channel);
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
