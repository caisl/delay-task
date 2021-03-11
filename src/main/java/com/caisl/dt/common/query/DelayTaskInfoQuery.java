package com.caisl.dt.common.query;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DelayTaskClockQuery
 *
 * @author caisl
 * @since 2021/3/02
 */
@Data
@Builder
public class DelayTaskInfoQuery {

    /**
     * 任务触发时间
     */
    private Long taskTriggerTime;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 任务分片ID
     */
    private Integer index;

    /**
     * 总分片数量
     */
    private Integer total;
}
