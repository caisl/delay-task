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
public class DelayTaskDTO implements Serializable {

    private static final long serialVersionUID = 4978517363556916182L;
    /**
     * 消息topic
     */
    private String topic;
    /**
     * 生产者组ID (跟topic一对一关系)
     */
    private String producerGroupId;
    /**
     * 消息tag
     */
    private String tag;

    /**
     * 任务参数，JSON串，透传
     */
    private String paramJson;
    /**
     * 业务应用名称
     */
    private String appName;

}
