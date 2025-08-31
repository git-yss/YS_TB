package org.ys.commens.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.ObjectInputFilter;

public class RedissonConfig {

    @Value("${redis.address}")
    private String redisAddress;

    @Value("${redis.password}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 集群模式配置（根据实际情况选择模式）
        // config.useClusterServers()
        //       .addNodeAddress(redisAddress)
        //       .setPassword(redisPassword);

        // 单节点模式
        config.useSingleServer()
                .setAddress(redisAddress)
                .setPassword(redisPassword)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(24)
                .setTimeout(3000);

        return Redisson.create(config);
    }
}
