package com.caisl.dt.common.dao;

import com.caisl.dt.common.dao.mapper.DelayTaskMapper;
import com.caisl.dt.common.dataobject.DelayTaskDO;
import com.caisl.dt.common.query.DelayTaskQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * DelayTaskDAO
 *
 * @author caisl
 * @since 2019-04-29
 */
@Repository
public class DelayTaskDAO {

    @Resource
    DelayTaskMapper delayTaskMapper;

    /**
     * 插入一条记录
     *
     * @param record
     * @return
     */
    public int insert(DelayTaskDO record){
        return delayTaskMapper.insert(record);
    }

    /**
     * 通过条件查询
     *
     * @param query
     * @return
     */
    public List<DelayTaskDO> queryListByQuery(DelayTaskQuery query){
        return delayTaskMapper.queryListByQuery(query);
    }

    /**
     * 根据主键查询
     *
     * @param delayTaskId
     * @return
     */
    public DelayTaskDO selectByPrimaryKey(Long delayTaskId){
        return delayTaskMapper.selectByPrimaryKey(delayTaskId);
    }

}
