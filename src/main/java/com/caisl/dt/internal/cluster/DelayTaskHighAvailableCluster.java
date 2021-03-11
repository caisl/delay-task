package com.caisl.dt.internal.cluster;

import com.caisl.dt.common.constant.DelayTaskClockStatusEnum;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.ExecutorBizImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caisl
 * @since 2020/7/17
 */
@Component
public class DelayTaskHighAvailableCluster implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //服务重启或者新节点加入，内存中任务丢失，需要重新加载到内存中执行
        List<Integer> loadStatus = new ArrayList<>(2);
        loadStatus.add(DelayTaskClockStatusEnum.PREPARE.getStatus());
        loadStatus.add(DelayTaskClockStatusEnum.LOAD.getStatus());
        ExecutorBiz executorBiz = new ExecutorBizImpl();
        ReturnT<String> result = executorBiz.run(getTriggerParam());
    }

    private TriggerParam getTriggerParam(){
        TriggerParam triggerParam = new TriggerParam();
        // 任务ID
        triggerParam.setJobId(59);
        // 任务标识
        triggerParam.setExecutorHandler("loadDelayTaskClockJob");
        // 任务参数
        triggerParam.setExecutorParams("手动触发任务");
        // 任务阻塞策略，可选值参考 com.xxl.job.core.enums.ExecutorBlockStrategyEnum
        triggerParam.setExecutorBlockStrategy(ExecutorBlockStrategyEnum.COVER_EARLY.name());
        // 任务模式，可选值参考 com.xxl.job.core.glue.GlueTypeEnum
        triggerParam.setGlueType(GlueTypeEnum.BEAN.name());
        // GLUE脚本代码
        triggerParam.setGlueSource(null);
        // GLUE脚本更新时间，用于判定脚本是否变更以及是否需要刷新
        triggerParam.setGlueUpdatetime(System.currentTimeMillis());
        // 本次调度日志ID
        triggerParam.setLogId(triggerParam.getJobId());
        // 本次调度日志时间
        triggerParam.setLogDateTime(System.currentTimeMillis());
        return triggerParam;

    }
}
