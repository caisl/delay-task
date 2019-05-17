package com.caisl.dt.common.dataobject;

import lombok.Builder;
import lombok.Data;

/**
 * DelayTaskDO
 *
 * @author caisl
 * @since 2019-04-29
 */
@Data
@Builder
public class DelayTaskDO extends BaseDO {
    private static final long serialVersionUID = -922701694483879599L;
    /**
     * 主键
     */
    private Long delayTaskId;

    /**
     * 分片ID
     */
    private Integer shardingId;

    /**
     * 消息topic
     */
    private String topic;

    /**
     * 消息tag
     */
    private String tag;

    /**
     * 任务参数
     */
    private String params;

    /**
     * 任务触发时间
     */
    private Long triggerTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 扩展属性
     */
    private String extendField;
}
