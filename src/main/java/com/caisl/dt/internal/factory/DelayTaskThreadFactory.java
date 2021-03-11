package com.caisl.dt.internal.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DTThreadFactory
 *
 * @author caisl
 * @since 2019-05-21
 */
public class DelayTaskThreadFactory implements ThreadFactory {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("DelayTaskTriggerThread-" + count.incrementAndGet());
        return t;
    }
}
