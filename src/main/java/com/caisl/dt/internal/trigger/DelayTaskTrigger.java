package com.caisl.dt.internal.trigger;

import com.caisl.dt.domain.DelayTaskMessage;
import com.caisl.dt.internal.queue.DelayTaskQueue;
import com.caisl.dt.system.util.DateUtil;

/**
 * DelayTaskTrigger
 *
 * @author caisl
 * @since 2019-05-09
 */
public class DelayTaskTrigger implements Runnable {
    private DelayTaskQueue delayTaskQueue = DelayTaskQueue.INSTANCE;

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        while (true) {
            try {
                DelayTaskMessage message = delayTaskQueue.take();
                System.out.println("时间：" + DateUtil.timeMillis4YYYY_MM_DD_HH_MM_SS(System.currentTimeMillis()) + "调度线程：" + Thread
                        .currentThread()
                        .getName
                                () +
                        "获取消息" + message.getDelayTaskId() + ":" + DateUtil.timeMillis4YYYY_MM_DD_HH_MM_SS(message.getTriggerTime
                        ()));
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
