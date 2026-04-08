package org.ys.transaction.Infrastructure.port;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.ys.transaction.domain.port.DistributedLockPort;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedissonDistributedLockAdapter implements DistributedLockPort {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public <T> T withLock(String lockKey, long waitTime, TimeUnit unit, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, unit);
            if (!locked) {
                throw new IllegalStateException("系统繁忙，请稍后重试");
            }
            return callback.doInLock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("系统繁忙，请稍后重试");
        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception ignored) {
                }
            }
        }
    }
}


