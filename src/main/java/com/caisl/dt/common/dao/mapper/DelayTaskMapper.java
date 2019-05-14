package com.caisl.dt.common.dao.mapper;

import com.caisl.dt.common.dataobject.DelayTaskDO;

/**
 * DelayTaskMapper
 *
 * @author caisl
 * @since 2019-04-29
 */
public interface DelayTaskMapper {
    int deleteByPrimaryKey(String delayTaskId);

    int insert(DelayTaskDO record);

    int insertSelective(DelayTaskDO record);

    DelayTaskDO selectByPrimaryKey(String delayTaskId);

    int updateByPrimaryKeySelective(DelayTaskDO record);

    int updateByPrimaryKey(DelayTaskDO record);
}
