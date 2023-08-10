package com.chengzhx.distributionLocker.service;

import org.redisson.api.*;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 10:58
 */
public interface DistributeLocker {
    /**
     * 获取锁
     *
     * @param lockKey 锁的key
     * @return RLock
     */
    RLock getLock(String lockKey);

    /**
     * 获取公平锁
     *
     * @param lockKey 锁的key
     * @return RLock
     */
    RLock getFairLock(String lockKey);

    /**
     * 获取读写锁
     *
     * @param lockKey 锁的key
     * @return RReadWriteLock
     */
    RReadWriteLock getReadWriteLock(String lockKey);

    /**
     * 获取信号量
     *
     * @param lockKey 锁的key
     * @return RSemaphore
     */
    RSemaphore getSemaphore(String lockKey);

    /**
     * 获取倒计时器
     *
     * @param lockKey 锁的key
     * @return RCountDownLatch
     */
    RCountDownLatch getCountDownLatch(String lockKey);

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的key
     * @param waitTime  等待时间
     * @param leaseTime 锁的过期时间
     * @param timeUnit  时间单位
     * @return true-获取成功 false-获取失败
     * @throws InterruptedException 中断异常
     */
    RFuture<Boolean> tryLockAsync(String lockKey, int waitTime, int leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     */
    void unLock(String lockKey);

    /**
     * 释放锁
     *
     * @param lock 锁
     */
    void unLock(RLock lock);

    /**
     * 加锁
     *
     * @param lockKey 锁的key
     * @param timeout 锁的过期时间
     * @return RLock
     */
    RLock lock(String lockKey, int timeout);

    /**
     * 加锁
     *
     * @param lockKey  锁的key
     * @param timeUnit 时间单位
     * @param timeout  锁的过期时间
     * @return RLock
     */
    RLock lock(String lockKey, TimeUnit timeUnit, int timeout);

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的key
     * @param waitTime  等待时间
     * @param leaseTime 锁的过期时间
     * @return true-获取成功 false-获取失败
     */
    boolean tryLock(String lockKey, int waitTime, int leaseTime);

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的key
     * @param timeUnit  时间单位
     * @param waitTime  等待时间
     * @param leaseTime 锁的过期时间
     * @return true-获取成功 false-获取失败
     */
    boolean tryLock(String lockKey, TimeUnit timeUnit, int waitTime, int leaseTime);

    /**
     * 锁是否被任意一个线程锁持有
     *
     * @param lockKey 锁的key
     * @return true-被锁 false-未被锁
     */
    boolean isLocked(String lockKey);

    /**
     * 查询当前线程是否保持此锁定
     *
     * @param lockKey 锁的key
     * @return true-被锁 false-未被锁
     */
    boolean isHeldByCurrentThread(String lockKey);
}
