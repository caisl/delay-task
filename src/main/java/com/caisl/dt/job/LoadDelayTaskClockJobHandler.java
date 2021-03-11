package com.caisl.dt.job;

import ch.qos.logback.classic.Level;
import com.caisl.dt.common.constant.DelayTaskClockStatusEnum;
import com.caisl.dt.internal.trigger.DelayTaskTriggerManager;
import com.caisl.dt.system.helper.ShardingItemHelper;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.LogUtil;
import com.caisl.dt.system.util.log.KVJsonFormat;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.ShardingUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caisl
 * @since 2020/7/22
 */
@Component
@JobHandler(value = "loadDelayTaskClockJob")
public class LoadDelayTaskClockJobHandler extends IJobHandler {
    @Resource
    private DelayTaskTriggerManager delayTaskTriggerManager;
    @Resource
    private ShardingItemHelper shardingItemHelper;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
        shardingItemHelper.initShardingParam(shardingVO.getIndex(), shardingVO.getTotal());
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_OPERATE, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("loadDelayTaskClockJob启动")
                .add("当前分片序号", shardingItemHelper.getIndex()).add("总分片数", shardingItemHelper.getIndexTotal())));
        List<Integer> clockStatusList = new ArrayList<>(1);
        clockStatusList.add(DelayTaskClockStatusEnum.PREPARE.getStatus());
        clockStatusList.add(DelayTaskClockStatusEnum.LOAD.getStatus());
        delayTaskTriggerManager.loadClock(clockStatusList);
        return ReturnT.SUCCESS;
    }
}
