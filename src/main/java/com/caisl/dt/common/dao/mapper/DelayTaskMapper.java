package com.caisl.dt.common.dao.mapper;

import com.caisl.dt.common.dataobject.DelayTaskDO;
import com.caisl.dt.common.query.DelayTaskQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * DelayTaskMapper
 *
 * @author caisl
 * @since 2019-04-29
 */
@Mapper
public interface DelayTaskMapper {
    int deleteByPrimaryKey(String delayTaskId);

    int insert(DelayTaskDO record);

    List<DelayTaskDO> queryListByQuery(DelayTaskQuery query);


    int insertSelective(DelayTaskDO record);

    DelayTaskDO selectByPrimaryKey(String delayTaskId);

    int updateByPrimaryKeySelective(DelayTaskDO record);

    int updateByPrimaryKey(DelayTaskDO record);
}
