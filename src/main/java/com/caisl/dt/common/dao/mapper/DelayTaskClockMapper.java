package com.caisl.dt.common.dao.mapper;


import com.caisl.dt.common.dataobject.DelayTaskClockDO;
import com.caisl.dt.common.query.DelayTaskClockQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * DelayTaskClock - Mapper接口
 *
 * @author caisl
 * @since 2021/3/02
 */
@Mapper
public interface DelayTaskClockMapper {

    /**
     * DelayTaskClock - 添加
     *
     * @param delayTaskClockDO 实体
     * @return 添加结果
     */
    int insert(DelayTaskClockDO delayTaskClockDO);

    /**
     * DelayTaskClock - 修改
     *
     * @param delayTaskClockDO 实体
     * @return 修改结果
     */
    int update(DelayTaskClockDO delayTaskClockDO);

    /**
     * DelayTaskClock - 删除
     *
     * @param id
     * @return 删除结果
     */
    int delete(Long id);

    /**
     * 根据PK查询
     *
     * @return DelayTaskClockDO
     */
    DelayTaskClockDO get(Long id);

    /**
     * 根据时钟周期查询
     *
     * @param clockTriggerTime
     * @return
     */
    DelayTaskClockDO queryByClockTriggerTime(Long clockTriggerTime);

    /**
     * queryListByQuery
     *
     * @param delayTaskClockQuery
     * @return
     */
    List<DelayTaskClockDO> queryListByQuery(DelayTaskClockQuery delayTaskClockQuery);


    int updateStatusBatch(List<DelayTaskClockDO> delayTaskClockDOS);


}