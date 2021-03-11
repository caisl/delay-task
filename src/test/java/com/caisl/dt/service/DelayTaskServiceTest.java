package com.caisl.dt.service;

import com.alibaba.fastjson.JSON;
import com.caisl.dt.BaseTest;
import com.caisl.dt.domain.DelayTaskDTO;
import org.junit.Test;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DelayTaskServiceTest
 *
 * @author caisl
 * @since 2019-05-08
 */
public class DelayTaskServiceTest extends BaseTest {
    @Resource
    IDelayTaskService delayTaskService;

    @Test
    public void simulateConcurrentAddTaskTest() {
        ExecutorService pool = Executors.newCachedThreadPool();
        CountDownLatch count = new CountDownLatch(10);
        Long triggerTime = System.currentTimeMillis() + 350000;
        for (int i = 0; i < 10; i++) { //启动线程
            MyRunnable myRunnable = new MyRunnable(count, triggerTime);
            pool.execute(myRunnable);
            count.countDown();
        }
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }


    public class MyRunnable implements Runnable {

        private CountDownLatch count;
        private Long triggerTime;

        public MyRunnable(CountDownLatch count, Long triggerTime) {
            this.count = count;
            this.triggerTime = triggerTime;
        }

        public void run() {
            try {
                count.await();
                DelayTaskDTO delayTaskDTO = new DelayTaskDTO();
                delayTaskDTO.setAppName("order-center");
                delayTaskDTO.setTopic("mjk_daily_order");
                delayTaskDTO.setTag("order_auto_timeout");
                delayTaskDTO.setParamJson("{\"order_id\":\"123456789\"}");
                delayTaskDTO.setProducerGroupId("GID_mjk_daily_order");
                delayTaskService.addTaskAbsoluteTime(triggerTime, delayTaskDTO);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
