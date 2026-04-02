package org.ys.transaction.domain.port;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁端口（基础设施实现：Redisson 等）
 */
public interface DistributedLockPort {

    <T> T withLock(String lockKey, long waitTime, TimeUnit unit, LockCallback<T> callback);

    interface LockCallback<T> {
        T doInLock();
    }
}
