package com.caisl.dt.job;

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson.JSON;
import com.caisl.dt.internal.handler.IDelayTaskHandler;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.LogUtil;
import com.caisl.dt.system.util.log.KVJsonFormat;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import lombok.Getter;
import lombok.Setter;
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
        Long beginTime = System.currentTimeMillis();
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.JOB, Level.INFO, "<====DelayTaskLoad" +
                " Job Begin====>" + beginTime);

        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.JOB, Level.INFO,
                LogUtil.formatLog(KVJsonFormat.title("DelayTaskLoadJob")
                        .add("thread_id", Thread.currentThread().getId())
                        .add("任务总片数", shardingContext.getShardingTotalCount())
                        .add("当前分片项", shardingContext.getShardingItem())));
        delayTaskHandler.loadTask();

        Long endTime = System.currentTimeMillis();
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.JOB, Level.INFO, "<====DelayTaskLoad" +
                " Job End====>" + endTime + "cost:" + (endTime - beginTime));


    }
}
