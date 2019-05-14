package com.caisl.dt.service;

import com.caisl.dt.BaseTest;
import com.caisl.dt.domain.AddDelayTaskDTO;
import org.junit.Test;

import javax.annotation.Resource;

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
    public void addTaskTest(){
        AddDelayTaskDTO taskDTO = AddDelayTaskDTO.builder().build();
        delayTaskService.addTask(taskDTO);




    }


}
