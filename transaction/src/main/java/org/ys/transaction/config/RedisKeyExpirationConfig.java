package org.ys.transaction.config;



import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.ys.transaction.listener.RedisKeyExpirationListener;

import javax.annotation.PostConstruct;

@Configuration
public class RedisKeyExpirationConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisKeyExpirationListener redisKeyExpirationListener;

    @PostConstruct
    public void registerListener() {
        // 注册Redis过期事件监听器
        redissonClient.getPatternTopic("__keyevent@0__:expired", new StringCodec()).addListener(String.class, redisKeyExpirationListener);
    }
}
