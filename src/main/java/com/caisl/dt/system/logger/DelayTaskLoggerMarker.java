package com.caisl.dt.system.logger;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * DelayTaskLoggerMarker
 *
 * @author caisl
 * @since 2019-04-24
 */
public class DelayTaskLoggerMarker {
    /**
     * 异常处理
     */
    public static final Marker EXCEPTION_HANDLER = MarkerFactory.getMarker("exception_handler");

    public static final Marker BUSINESS = MarkerFactory.getMarker("business");

    public static final Marker DELAY_TASK_MSG = MarkerFactory.getMarker("delay_task_message");
    public static final Marker JOB = MarkerFactory.getMarker("job");
}
