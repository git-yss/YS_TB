package org.ys.transaction.service.impl;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀Lua脚本执行器
 * 使用Lua脚本实现原子性秒杀操作，提升性能和一致性
 */
@Component
public class SeckillLuaScript {

    private static final String STOCK_PREFIX = "seckill:stock";
    private static final String USER_ORDER_PREFIX = "seckill:user:order:";
    private static final String USER_RECORD_PREFIX = "seckill:user:record:";

    @Resource
    private RedissonClient redissonClient;

    private String seckillLuaScript;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("lua/seckill.lua");
            this.seckillLuaScript = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load seckill Lua script", e);
        }
    }

    /**
     * 执行秒杀Lua脚本（原子操作）
     *
     * @param itemId    商品ID
     * @param userId    用户ID
     * @param orderTimeOut 订单过期时间（分钟）
     * @param itemJson  商品JSON数据
     * @return 执行结果
     */
    public Object executeSeckill(String itemId, String userId, long orderTimeOut, String itemJson) {
        String stockKey = STOCK_PREFIX;
        String userOrderKey = USER_ORDER_PREFIX + userId;
        String userRecordKey = USER_RECORD_PREFIX + userId;

        RScript script = redissonClient.getScript(new StringCodec());

        return script.eval(
                RScript.Mode.READ_WRITE,
                seckillLuaScript,
                RScript.ReturnType.VALUE,
                Collections.emptyList(),
                stockKey,
                userOrderKey,
                userRecordKey,
                itemId,
                userId,
                String.valueOf(orderTimeOut),
                itemJson
        );
    }

    /**
     * 初始化秒杀商品库存
     *
     * @param itemId 商品ID
     * @param stock   库存数量
     */
    public void initSeckillStock(String itemId, int stock) {
        String stockCountKey = STOCK_PREFIX + ":count:" + itemId;
        redissonClient.getBucket(stockCountKey).set(stock, 1, TimeUnit.HOURS);
    }

    /**
     * 获取秒杀商品剩余库存
     *
     * @param itemId 商品ID
     * @return 剩余库存数量
     */
    public long getSeckillStock(String itemId) {
        String stockCountKey = STOCK_PREFIX + ":count:" + itemId;
        Object stock = redissonClient.getBucket(stockCountKey).get();
        return stock != null ? Long.parseLong(stock.toString()) : 0;
    }
}
