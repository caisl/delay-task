package com.caisl.dt.service;

import com.caisl.dt.domain.Result;
import com.caisl.dt.domain.AddDelayTaskDTO;

import javax.validation.Valid;

/**
 * IDelayTaskService
 *
 * @author caisl
 * @since 2019-05-07
 */
public interface IDelayTaskService {

    /**
     * 添加延迟任务
     *
     * @param addDelayTaskDTO
     * @return
     */
    Result<Long> addTask(@Valid AddDelayTaskDTO addDelayTaskDTO);

    /**
     * 取消延迟任务
     *
     * @param taskId
     * @return
     */
    Result<Boolean> cancelTask(Long taskId);

}
