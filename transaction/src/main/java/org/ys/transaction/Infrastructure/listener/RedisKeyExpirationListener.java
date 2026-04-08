package org.ys.transaction.Infrastructure.listener;

import org.redisson.api.listener.PatternMessageListener;
import org.springframework.stereotype.Component;
import org.ys.transaction.domain.service.CartTimeoutCompensationService;

import jakarta.annotation.Resource;

@Component
public class RedisKeyExpirationListener implements PatternMessageListener<String> {

    @Resource
    private CartTimeoutCompensationService cartTimeoutCompensationService;

    @Override
    public void onMessage(CharSequence charSequence, CharSequence charSequence1, String expiredKey) {
        cartTimeoutCompensationService.onRedisKeyExpired(expiredKey);
    }
}

