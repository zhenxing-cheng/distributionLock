package com.chengzhx.distributionLocker.service;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/16 14:04
 */
public interface UserContextService {
    /**
     * 获取当前用户id
     *
     * @return 当前用户id
     */
    Integer getUserId();
}
