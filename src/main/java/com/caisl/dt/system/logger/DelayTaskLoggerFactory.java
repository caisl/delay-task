package com.caisl.dt.system.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DelayTaskLoggerFactory
 *
 * @author caisl
 * @since 2019-04-24
 */
public class DelayTaskLoggerFactory {
    /**
     * 异常日志
     */
    public final static Logger EXCEPTION_HANDLER = LoggerFactory.getLogger("EXCEPTION_HANDLER");

    /**
     * 业务日志
     */
    public final static Logger BUSINESS = LoggerFactory.getLogger("BUSINESS");

    /**
     * 消息日志
     */
    public final static Logger MQ = LoggerFactory.getLogger("MQ");

}
