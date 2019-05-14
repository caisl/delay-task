package com.caisl.dt.system.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ZookeeperConfig
 *
 * @author caisl
 * @since 2019-04-24
 */
@Configuration
public class ZookeeperConfig {
    @Value("${zookeeper.serviceLists}")
    private String serviceLists;

    @Value("${zookeeper.namespace}")
    private String nameSpace;

    @Value("${zookeeper.baseSleepTimeMilliseconds}")
    private int baseSleepTimeMilliseconds;

    @Value("${zookeeper.maxSleepTimeMilliseconds}")
    private int maxSleepTimeMilliseconds;

    @Value("${zookeeper.maxRetries}")
    private int maxRetries;

    /**
     * zookeeper 配置
     *
     * @return
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter zookeeperRegistryCenter() {
        ZookeeperConfiguration configuration = new ZookeeperConfiguration(serviceLists, nameSpace);
        configuration.setBaseSleepTimeMilliseconds(baseSleepTimeMilliseconds);
        configuration.setMaxSleepTimeMilliseconds(maxSleepTimeMilliseconds);
        configuration.setMaxRetries(maxRetries);
        return new ZookeeperRegistryCenter(configuration);
    }
}
