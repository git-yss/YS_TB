package org.ys.transaction.domain.service;

/**
 * 订单超时补偿（领域服务）：由基础设施（Redis 过期事件）触发调用。
 */
public interface CartTimeoutCompensationService {

    void onRedisKeyExpired(String expiredKey);
}

