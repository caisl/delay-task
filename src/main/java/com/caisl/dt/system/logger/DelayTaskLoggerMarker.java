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
}
