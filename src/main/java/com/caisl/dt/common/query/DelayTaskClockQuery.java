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
public class DelayTaskClockQuery {
    /**
     * 查询开始ID，用于减少遍历数据量
     */
    private Long beginId;

    /**
     * 任务触发时间
     */
    private Long taskTriggerTime;

    /**
     * 任务补偿时间
     */
    private Long compensateTime;
    /**
     * 分页数据大小
     */
    private Integer pageSize;

    private List<Integer> clockStatusList;
}
