package com.caisl.dt.common.dataobject;

import lombok.Data;

/**
 *
 *
 * @author caisl
 * @since 2021/3/02
 */
@Data
public class DelayTaskInfoDO extends BaseDO {

    private static final long serialVersionUID = 5010459880829208669L;
    /**
     * 业务方应用名称
     */
    private String appName;
    /**
     * 消息topic
     */
    private String topic;
    /**
     * 消息tag
     */
    private String tag;
    /**
     * 生产者组ID
     */
    private String producerGroupId;
    /**
     * 参数
     */
    private String params;
    /**
     * 执行时间
     */
    private Long taskTriggerTime;
    /**
     * 任务状态：1.初始化 2.消息已发放 3.处理成功 4.处理失败 5.任务取消
     */
    private Integer taskStatus;
    /**
     * 消息ID
     */
    private String msgId;
    /**
     * 扩展属性
     */
    private String extendField;
}
