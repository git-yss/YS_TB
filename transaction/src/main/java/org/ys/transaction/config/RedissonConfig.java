package org.ys.transaction.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


@Configuration
public class RedissonConfig {

    @Value("${redis.address}")
    private String redisAddress;

    @Value("${redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        // 集群模式配置（根据实际情况选择模式）
        // config.useClusterServers()
        //       .addNodeAddress(redisAddress)
        //       .setPassword(redisPassword);

        // 单节点模式
        config.useSingleServer()
                .setAddress(redisAddress)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(24)
                .setTimeout(3000);
        // 只有当密码不为空时才设置密码
        if (StringUtils.hasText(redisPassword)) {
            config.useSingleServer().setPassword(redisPassword);
        }
        return Redisson.create(config);
    }
}
