package com.caisl.dt.service;

import com.caisl.dt.BaseTest;
import com.caisl.dt.domain.AddDelayTaskDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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
    public void testAddTask4AddOneTaskIntoDelayQueue() {
        AddDelayTaskDTO taskDTO = AddDelayTaskDTO.builder().topic("topic").tag("tag").paramJson(StringUtils.EMPTY)
                .delayTime(5L).timeUnit(TimeUnit.SECONDS).build();
        Assert.assertTrue(delayTaskService.addTask(taskDTO).isSuccess());
        try {
            Thread.sleep(6000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void addTaskTest() throws InterruptedException {
        for (int i = 0; i <= 10; i++) {
            AddDelayTaskDTO taskDTO = AddDelayTaskDTO.builder().topic("topic").tag("tag").paramJson(StringUtils.EMPTY)
                    .delayTime(5L).timeUnit(TimeUnit.SECONDS).build();

            delayTaskService.addTask(taskDTO);
        }

        for (int i = 0; i <= 10; i++) {
            AddDelayTaskDTO taskDTO = AddDelayTaskDTO.builder().topic("topic").tag("tag").paramJson(StringUtils.EMPTY)
                    .delayTime(10L).timeUnit(TimeUnit.SECONDS).build();

            delayTaskService.addTask(taskDTO);
        }


        Thread.sleep(1000000L);

    }


}
