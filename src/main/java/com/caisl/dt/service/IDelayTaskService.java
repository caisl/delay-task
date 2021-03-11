package com.caisl.dt.service;

import com.caisl.dt.domain.Result;
import com.caisl.dt.domain.DelayTaskDTO;

import java.util.concurrent.TimeUnit;

/**
 * IDelayTaskService
 *
 * @author caisl
 * @since 2019-05-07
 */
public interface IDelayTaskService {
    /**
     * 添加延迟任务（动态时间）
     *
     * @param delayTime
     * @param timeUnit
     * @param delayTaskDTO
     * @return delayTaskId
     */
    Result<Long> addTaskDelayDynamicTime(Long delayTime, TimeUnit timeUnit, DelayTaskDTO delayTaskDTO);

    /**
     * 添加延迟任务（绝对时间）
     *
     * @param absoluteTime 时间戳
     * @param delayTaskDTO
     * @return delayTaskId
     */
    Result<Long> addTaskAbsoluteTime(Long absoluteTime, DelayTaskDTO delayTaskDTO);

    /**
     * 取消延迟任务
     *
     * @param delayTaskId
     * @return
     */
    Result<Boolean> cancelTask(Long delayTaskId);

    /**
     * 延迟任务处理结果通知
     *
     * @param delayTaskId
     * @param isSuccess
     * @return
     */
    Result<Boolean> handleResultNotify(Long delayTaskId, Boolean isSuccess);

}
