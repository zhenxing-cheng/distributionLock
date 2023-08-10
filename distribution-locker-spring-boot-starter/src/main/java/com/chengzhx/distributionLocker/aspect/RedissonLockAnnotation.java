package com.chengzhx.distributionLocker.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 11:15
 */
@Target(ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface RedissonLockAnnotation {

    /**
     * 锁的名称（key）
     * 如果不设置，默认为方法名
     *
     * @return 锁的名称
     */
    String lockName() default "";

    /**
     * 等待时间 单位：秒， 默认10秒
     *
     * @return 等待时间
     */
    int waitTime() default 10;

    /**
     * 持有锁的时间 单位：秒， 默认30秒
     *
     * @return 持有锁的时间
     */
    int leaseTime() default 30;

    /**
     * 是否使用操作人id作为key的一部分 默认否
     * <p>
     * 如果选择true,在lockName的后面会拼接上":operatorId:" + userId
     * userId将从SecurityContextHolder.getContext().getAuthentication()中获取
     * @see com.intelliquor.cloud.shop.common.model.UserContext
     *
     * @return 是否使用操作人id作为key的一部分
     */
    boolean useOperatorIdKey() default false;
}
