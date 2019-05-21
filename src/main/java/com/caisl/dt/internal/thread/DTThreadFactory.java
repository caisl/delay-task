package com.caisl.dt.internal.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DTThreadFactory
 *
 * @author caisl
 * @since 2019-05-21
 */
public class DTThreadFactory implements ThreadFactory {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("DelayTaskTrigger-" + count.incrementAndGet());
        return t;
    }
}
