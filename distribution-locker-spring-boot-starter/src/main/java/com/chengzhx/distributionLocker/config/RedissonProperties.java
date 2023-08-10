package com.chengzhx.distributionLocker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 09:19
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    private String address;

    private String password;

    private int timeout = 3000;

    private int connectionPoolSize = 64;

    private int connectionMinimumIdleSize = 10;

    private int slaveConnectionPoolSize = 250;

    private int masterConnectionPoolSize = 250;

    private String[] sentinelAddresses;

    private String masterName;

    private String[] masterAddresses;

    //集群状态扫描间隔时间，单位是毫秒
    private int scanInterval=2000;
}
