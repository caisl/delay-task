package com.caisl.dt.common.constant;

/**
 * DelayTaskConstant
 *
 * @author caisl
 * @since 2019-05-17
 */
public class DelayTaskConstant {

    public final static int LOAD_JOB_TRIGGER_PERIOD = 5 * 60 * 1000;

    public final static int DELAY_TASK_COMPENSATE_TIME = 5 * 60 * 1000;

    public final static int TASK_CLOCK_GRANULARITY_1_SECOND = 1 * 1000;
    /**
     * 任务循环每页大小
     */
    public final static int TASK_LOOP_PAGE_SIZE = 100;

    /**
     * 任务循环次数最大值
     */
    public final static int MAX_TASK_LOOP_INDEX = 200;
}
