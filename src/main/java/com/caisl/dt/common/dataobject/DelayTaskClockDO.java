package com.caisl.dt.common.dataobject;

import lombok.Data;


/**
 * DelayTaskClock
 *
 * @author caisl
 * @since 2021/3/02
 */
@Data
public class DelayTaskClockDO extends BaseDO {

    private static final long serialVersionUID = 4567582374248772921L;
    /**
     * 执行时间
     */
    private Long taskTriggerTime;
    /**
     * 任务状态：1.未处理 2.时钟已加载 3.执行 4.过期不再执行 5.过期执行
     */
    private Integer clockStatus;

    /**
     * 处理任务的机器分片信息
     */
    private String handleTaskShardingInfo;
}
