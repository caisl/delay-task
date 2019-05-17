package com.caisl.dt.common.query;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DelayTaskQuery
 *
 * @author caisl
 * @since 2019-05-17
 */
@Data
@Builder
public class DelayTaskQuery implements Serializable {
    /**
     * 查询开始ID，用于减少遍历数据量
     */
    private Long beginId;
    /**
     * 分片ID
     */
    private List<Integer> shardingIds;
    /**
     * 任务触发时间
     */
    private Long triggerTime;

    /**
     * 任务补偿时间
     */
    private Long compensateTime;
    /**
     * 分页数据大小
     */
    private Integer pageSize;
}
