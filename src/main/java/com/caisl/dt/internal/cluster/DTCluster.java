package com.caisl.dt.internal.cluster;

import com.caisl.dt.job.DelayTaskLoadConfig;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DTCluster
 *
 * @author caisl
 * @since 2019-05-15
 */
@Component
public class DTCluster implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private ZookeeperRegistryCenter regCenter;

    @Resource
    private DelayTaskLoadConfig delayTaskLoadConfig;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        //1.服务重启或者新节点加入，内存中任务丢失，需要重新加载到内存中执行
        new JobOperateAPIImpl(regCenter).trigger(Optional.of(delayTaskLoadConfig.getDelayTaskLoadJobName()), Optional.absent());
    }
}
