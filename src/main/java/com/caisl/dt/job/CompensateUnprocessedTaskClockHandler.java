package com.caisl.dt.job;

import com.caisl.dt.common.constant.DelayTaskClockStatusEnum;
import com.caisl.dt.internal.trigger.DelayTaskTriggerManager;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caisl
 * @since 2020/7/22
 */
@Component
@JobHandler(value = "compensateUnprocessedTaskClockHandler")
public class CompensateUnprocessedTaskClockHandler extends IJobHandler {
    @Resource
    private DelayTaskTriggerManager delayTaskTriggerManager;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        List<Integer> clockStatusList = new ArrayList<>(2);
        clockStatusList.add(DelayTaskClockStatusEnum.PREPARE.getStatus());
        clockStatusList.add(DelayTaskClockStatusEnum.LOAD.getStatus());
        clockStatusList.add(DelayTaskClockStatusEnum.EXECUTE.getStatus());
        delayTaskTriggerManager.compensateClock(clockStatusList);
        return ReturnT.SUCCESS;
    }
}
