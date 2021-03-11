package com.caisl.dt.common.dao;

import com.caisl.dt.common.dao.mapper.DelayTaskClockMapper;
import com.caisl.dt.common.dataobject.DelayTaskClockDO;
import com.caisl.dt.common.query.DelayTaskClockQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * DelayTaskClockDAO
 *
 * @author caisl
 * @since 2021/3/02
 */
@Repository
public class DelayTaskClockDAO {
    @Resource
    private DelayTaskClockMapper delayTaskClockMapper;

    public int updateStatusBatch(List<DelayTaskClockDO> delayTaskClockDOS){
        return delayTaskClockMapper.updateStatusBatch(delayTaskClockDOS);
    }
    /**
     * queryListByQuery
     *
     * @param delayTaskClockQuery
     * @return
     */
    public List<DelayTaskClockDO> queryListByQuery(DelayTaskClockQuery delayTaskClockQuery){
        return delayTaskClockMapper.queryListByQuery(delayTaskClockQuery);
    }
    /**
     * 插入一条数据
     *
     * @param delayTaskClockDO 实体
     * @return 是否成功
     */
    public boolean insert(DelayTaskClockDO delayTaskClockDO) {
        int result = delayTaskClockMapper.insert(delayTaskClockDO);
        return result > 0;
    }

    /**
     * 更新
     *
     * @param delayTaskClockDO 实体
     * @return 是否成功
     */
    public boolean update(DelayTaskClockDO delayTaskClockDO) {
        int result = delayTaskClockMapper.update(delayTaskClockDO);
        return result > 0;
    }

    /**
     * 删除
     *
     * @param id
     * @return 是否成功
     */
    public boolean delete(Long id) {
        int result = delayTaskClockMapper.delete(id);
        return result > 0;
    }

    /**
     * getDelayTaskClockDO
     *
     * @param id
     * @return DelayTaskClockDO
     */
    public DelayTaskClockDO get(Long id) {
        DelayTaskClockDO delayTaskClock = delayTaskClockMapper.get(id);
        return delayTaskClock;
    }

    /**
     * queryByTaskTriggerTime
     *
     * @param clockTriggerTime
     * @return DelayTaskClockDO
     */
    public DelayTaskClockDO queryByTaskTriggerTime(Long clockTriggerTime) {
        DelayTaskClockDO delayTaskClock = delayTaskClockMapper.queryByClockTriggerTime(clockTriggerTime);
        return delayTaskClock;
    }

}