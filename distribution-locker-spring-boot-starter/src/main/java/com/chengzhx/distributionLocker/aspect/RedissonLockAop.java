package com.chengzhx.distributionLocker.aspect;

import com.chengzhx.distributionLocker.exception.DistributionLockException;
import com.chengzhx.distributionLocker.service.UserContextService;
import com.chengzhx.distributionLocker.util.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 11:16
 */
@Slf4j
@Aspect
@Component
public class RedissonLockAop {

    private final UserContextService userContextService;

    public RedissonLockAop(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    /**
     * 定义切点，拦截被 @RedissonLockAnnotation 修饰的方法
     */
    @Pointcut("@annotation(com.chengzhx.distributionLocker.aspect.RedissonLockAnnotation)")
    public void redissonLockAspect() {
    }

    @Around("redissonLockAspect()")
    public Object checkLock(ProceedingJoinPoint joinPoint) throws Throwable {
        // 当前线程名称
        String currentThreadName = Thread.currentThread().getName();
        log.info("线程{}------进入分布式锁aop------", currentThreadName);
        // 获取注解参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RedissonLockAnnotation redissonLockAnnotation = signature.getMethod().getAnnotation(RedissonLockAnnotation.class);
        String lockName = redissonLockAnnotation.lockName();
        int waitTime = redissonLockAnnotation.waitTime();
        int leaseTime = redissonLockAnnotation.leaseTime();
        boolean useOperatorIdKey = redissonLockAnnotation.useOperatorIdKey();
        // 获取锁
        if (StringUtils.isBlank(lockName)) {
            log.info("线程{}------分布式锁未设置锁的key------", currentThreadName);
            lockName = signature.getMethod().getName();
        }
        try {
            String baseLockKey = "DistributedLock:";
            lockName = baseLockKey + lockName;
            if (useOperatorIdKey) {
                Integer userId = userContextService.getUserId();
                log.info("使用操作人id:[{}]作为key的一部分", userId);
                lockName = lockName + ":operatorId:" + userId;
            }
            log.info("线程{}------分布式锁设置锁的key为{}------", currentThreadName, lockName);
            log.info("key为[{}]分布式锁的等待时间为[{}]秒，持有锁的时间为[{}]秒, 是否使用操作人id作为key[{}]", lockName, waitTime, leaseTime, useOperatorIdKey);
            // 尝试获取锁
            if (RedissonLockUtil.tryLock(lockName, TimeUnit.SECONDS, waitTime, leaseTime)) {
                // 获取锁成功
                log.info("线程{}------获取分布式锁成功------", currentThreadName);
                return joinPoint.proceed();
            } else {
                // 获取锁失败
                log.error("线程{}------获取分布式锁失败------", currentThreadName);
                throw new DistributionLockException("获取分布式锁失败");
            }
        } finally {
            if (RedissonLockUtil.isLocked(lockName)) {
                log.info("线程{}------释放分布式锁------", currentThreadName);
                // 释放锁
                if (RedissonLockUtil.isHeldByCurrentThread(lockName)) {
                    log.info("线程{}------当前线程持有分布式锁，释放锁------", currentThreadName);
                    RedissonLockUtil.unLock(lockName);
                    log.info("线程{}------释放分布式锁成功------", currentThreadName);
                }
            }
        }
    }
}
