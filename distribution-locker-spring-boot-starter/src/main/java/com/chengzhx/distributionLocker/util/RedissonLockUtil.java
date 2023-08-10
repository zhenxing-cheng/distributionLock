package com.chengzhx.distributionLocker.util;

import org.redisson.api.*;
import com.chengzhx.distributionLocker.service.DistributeLocker;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 09:30
 */
public class RedissonLockUtil {

    private static DistributeLocker locker;

    public static void setLocker(DistributeLocker locker) {
        RedissonLockUtil.locker = locker;
    }

    public static RLock getLock(String lockKey) {
        return locker.getLock(lockKey);
    }

    public static RLock getFairLock(String lockKey) {
        return locker.getFairLock(lockKey);
    }

    public static RReadWriteLock getReadWriteLock(String lockKey) {
        return locker.getReadWriteLock(lockKey);
    }

    public static RSemaphore getSemaphore(String lockKey) {
        return locker.getSemaphore(lockKey);
    }


    public static RCountDownLatch getCountDownLatch(String lockKey) {
        return locker.getCountDownLatch(lockKey);
    }

    public static RFuture<Boolean> tryLockAsync(String lockKey, int waitTime, int leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = locker.getLock(lockKey);
        Thread.sleep(1000);
        return lock.tryLockAsync(waitTime, leaseTime, timeUnit);
    }

    public static void unLock(String lockKey) {
        RLock lock = locker.getLock(lockKey);
        lock.unlock();
    }

    public static void unLock(RLock lock) {
        lock.unlock();
    }

    public static RLock lock(String lockKey, int timeout) {
        RLock lock = locker.getLock(lockKey);
        lock.lock(timeout, TimeUnit.SECONDS);
        return lock;
    }

    public static RLock lock(String lockKey, TimeUnit timeUnit, int timeout) {
        RLock lock = locker.getLock(lockKey);
        lock.lock(timeout, timeUnit);
        return lock;
    }

    public static boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        RLock lock = locker.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static boolean tryLock(String lockKey, TimeUnit timeUnit, int waitTime, int leaseTime) {
        RLock lock = locker.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static boolean isLocked(String lockKey) {
        RLock lock = locker.getLock(lockKey);
        return lock.isLocked();
    }

    public static boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = locker.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}