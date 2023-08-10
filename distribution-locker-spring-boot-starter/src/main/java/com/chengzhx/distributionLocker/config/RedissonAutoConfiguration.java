package com.chengzhx.distributionLocker.config;

import com.chengzhx.distributionLocker.util.RedissonLockUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.chengzhx.distributionLocker.service.impl.RedissonDistributeLocker;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 09:25
 */
@Configuration
@ConditionalOnClass(Config.class)
@AutoConfigureAfter(value = {RedissonProperties.class})
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonAutoConfiguration {

    private final RedissonProperties redissonProperties;

    public RedissonAutoConfiguration(RedissonProperties redissonProperties) {
        this.redissonProperties = redissonProperties;
    }

    /**
     * 单机模式自动装配
     *
     * @return RedissonClient
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.address")
    RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(redissonProperties.getAddress())
                .setTimeout(redissonProperties.getTimeout())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());
        if (StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }

    /**
     * 集群模式自动装配
     *
     * @return RedissonClient
     */
    @Bean
    @ConditionalOnProperty(value = "redisson.masterAddresses")
    public RedissonClient redissonSentinel() {
        Config config = new Config();
        ClusterServersConfig serverConfig = config.useClusterServers().addNodeAddress(redissonProperties.getMasterAddresses())
                .setTimeout(redissonProperties.getTimeout())
                //设置集群扫描时间
                .setScanInterval(redissonProperties.getScanInterval())
                //主节点线程池数量
                .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                //从节点线程池数量
                .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize());

        if (StringUtils.isNotEmpty(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    RedissonDistributeLocker redissonLocker(RedissonClient redissonClient) {
        RedissonDistributeLocker locker = new RedissonDistributeLocker(redissonClient);
        RedissonLockUtil.setLocker(locker);
        return locker;
    }
}
