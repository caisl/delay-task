package com.caisl.dt.internal.handler;

import com.caisl.dt.domain.DelayTaskMessage;

/**
 * IDelayTaskHandler
 *
 * @author caisl
 * @since 2019-05-09
 */
public interface IDelayTaskHandler {
    /**
     * 加载任务
     *
     * @return
     */
    boolean loadTask();

    /**
     * 处理任务
     *
     * @return
     */
    boolean dealTask(DelayTaskMessage delayTaskMessage);


}
