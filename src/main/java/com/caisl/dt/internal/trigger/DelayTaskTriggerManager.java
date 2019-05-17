package com.caisl.dt.internal.trigger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

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
            new ArrayBlockingQueue<Runnable>(200),
            new ThreadFactory() {
                private AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("DelayTaskTrigger-" + count.incrementAndGet());
                    return t;
                }
            }, new ThreadPoolExecutor.DiscardPolicy());


    @Override
    public void afterPropertiesSet(){
        //延迟队列 任务消费线程加载，线程全部阻塞在take方法。不需要创建太多消费线程
        threadPool.execute(new DelayTaskTrigger());
        threadPool.execute(new DelayTaskTrigger());
        threadPool.execute(new DelayTaskTrigger());
        threadPool.execute(new DelayTaskTrigger());
    }
}
