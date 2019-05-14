package com.caisl.dt.job;

import ch.qos.logback.classic.Level;
import com.caisl.dt.internal.handler.IDelayTaskHandler;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.LogUtil;
import com.caisl.dt.system.util.log.KVJsonFormat;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DemoJob
 *
 * @author caisl
 * @since 2019-04-24
 */
@Component
public class DelayTaskLoadJob implements SimpleJob {

    @Resource
    IDelayTaskHandler delayTaskHandler;

    @Override
    public void execute(ShardingContext shardingContext) {
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.BUSINESS, Level.INFO, "<====DelayTaskLoad Job Begin====>");

        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.BUSINESS, Level.INFO,
                LogUtil.formatLog(KVJsonFormat.title("DelayTaskLoadJob")
                        .add("thread_id", Thread.currentThread().getId())
                        .add("任务总片数", shardingContext.getShardingTotalCount())
                        .add("当前分片项", shardingContext.getShardingItem())));
        delayTaskHandler.loadTask();

        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.BUSINESS, Level.INFO, "<====DelayTaskLoad Job End====>");
    }
}
