package com.caisl.dt.job;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * DemoConfig
 *
 * @author caisl
 * @since 2019-04-24
 */
@Configuration
public class DelayTaskLoadConfig {
    @Resource
    private ZookeeperRegistryCenter registryCenter;

    public String getDelayTaskLoadJobName() {
        return delayTaskLoadJobName;
    }

    @Value("${delayTaskLoadJob.name}")
    private String delayTaskLoadJobName;

    @Value("${delayTaskLoadJob.cron}")
    private String delayTaskLoadJobCron;

    @Value("${delayTaskLoadJob.shardingTotalCount}")
    private int delayTaskLoadJobShardingTotalCount;

    @Value("${delayTaskLoadJob.shardingItemParameters}")
    private String shardingItemParameters;

    @Bean(initMethod = "init")
    public JobScheduler delayTaskLoadJobScheduler(final DelayTaskLoadJob delayTaskLoadJob) {
        return new SpringJobScheduler(delayTaskLoadJob, registryCenter, liteJobConfiguration());
    }

    private LiteJobConfiguration liteJobConfiguration() {
        return LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(
                        JobCoreConfiguration.newBuilder(delayTaskLoadJobName, delayTaskLoadJobCron, delayTaskLoadJobShardingTotalCount)
                                .shardingItemParameters(shardingItemParameters).build()
                        , DelayTaskLoadJob.class.getCanonicalName()
                )).overwrite(true).build();
    }

    public String getShardingItemParameters(){
        return this.shardingItemParameters;
    }
}
