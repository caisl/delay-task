package com.caisl.dt.internal.trigger;

import ch.qos.logback.classic.Level;
import com.caisl.dt.domain.DelayTaskMessage;
import com.caisl.dt.internal.handler.IDelayTaskHandler;
import com.caisl.dt.internal.queue.DelayTaskQueue;
import com.caisl.dt.internal.thread.DTThreadFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.DateUtil;
import com.caisl.dt.system.util.LogUtil;
import com.caisl.dt.system.util.log.KVJsonFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DelayTaskTriggerManager
 *
 * @author caisl
 * @since 2019-05-14
 */
@Component
public class DelayTaskTriggerManager implements InitializingBean {
    final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200),
            new DTThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());
    @Resource
    private IDelayTaskHandler delayTaskHandler;

    @Override
    public void afterPropertiesSet() {
        //延迟队列 任务消费线程加载，线程全部阻塞在take方法。不需要创建太多消费线程
        threadPool.execute(new DelayTaskTrigger());
        threadPool.execute(new DelayTaskTrigger());
        threadPool.execute(new DelayTaskTrigger());
        threadPool.execute(new DelayTaskTrigger());
    }

    /**
     * 延迟任务调度线程
     */
    private class DelayTaskTrigger implements Runnable {
        private DelayTaskQueue delayTaskQueue = DelayTaskQueue.INSTANCE;

        @Override
        public void run() {
            while (true) {
                try {
                    DelayTaskMessage message = delayTaskQueue.take();
                    LogUtil.log(DelayTaskLoggerFactory.MQ, DelayTaskLoggerMarker.DELAY_TASK_MSG, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("sendMsg")
                            .add("当前时间：", DateUtil.timeMillis4YYYY_MM_DD_HH_MM_SS(System.currentTimeMillis()))
                            .add("任务调度线程：", Thread.currentThread().getName())
                            .add("获取到延迟任务：", message.getDelayTaskId())
                            .add("任务触发时间：", DateUtil.timeMillis4YYYY_MM_DD_HH_MM_SS(message.getTriggerTime()))));
                    //可以考虑交给异步线程处理后续逻辑
                    delayTaskHandler.dealTask(message);
                } catch (InterruptedException e) {
                    LogUtil.log(DelayTaskLoggerFactory.MQ, DelayTaskLoggerMarker.DELAY_TASK_MSG, Level.INFO,
                            "DelayTaskTrigger error", e);
                }
            }
        }
    }
}
