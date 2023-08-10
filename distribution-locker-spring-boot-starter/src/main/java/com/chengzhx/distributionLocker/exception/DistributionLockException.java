package com.chengzhx.distributionLocker.exception;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/8/10 17:01
 */
public class DistributionLockException extends RuntimeException {

    public DistributionLockException(String message) {
        super(message);
    }
}
