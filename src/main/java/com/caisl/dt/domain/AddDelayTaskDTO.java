package com.caisl.dt.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * AddDelayTaskDTO
 *
 * @author caisl
 * @since 2019-05-08
 */
@Data
@Builder
public class AddDelayTaskDTO implements Serializable {

    private static final long serialVersionUID = 4978517363556916182L;
    /**
     * 消息topic
     */
    private String topic;
    /**
     * 消息tag
     */
    private String tag;
    /**
     * 延迟时间
     */
    private Long delayTime;
    /**
     * 延迟时间粒度
     */
    private TimeUnit timeUnit;
    /**
     * 任务参数，JSON串，透传
     */
    private String paramJson;

}
