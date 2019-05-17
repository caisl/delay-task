package com.caisl.dt.internal.queue;

import com.caisl.dt.domain.DelayTaskMessage;

import java.util.concurrent.DelayQueue;

/**
 * DelayTaskQueue
 *
 * @author caisl
 * @since 2019-05-09
 */
public enum DelayTaskQueue {
    INSTANCE;

    private DelayQueue<DelayTaskMessage> queue;

    DelayTaskQueue() {
        queue = new DelayQueue<>();
    }

    /**
     * 往队列中添加任务
     *
     * @param message
     * @return
     */
    public boolean add(DelayTaskMessage message) {
        return queue.add(message);
    }

    /**
     * 获取即将到期的任务
     *
     * @return
     * @throws InterruptedException
     */
    public DelayTaskMessage take() throws InterruptedException {
        return queue.take();
    }
}
