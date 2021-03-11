package com.caisl.dt.common.dao;


import com.caisl.dt.common.dao.mapper.DelayTaskInfoMapper;
import com.caisl.dt.common.dataobject.DelayTaskInfoDO;
import com.caisl.dt.common.query.DelayTaskInfoQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * DelayTaskInfoDAO
 *
 * @author caisl
 * @since 2021/3/02
 */
@Repository
public class DelayTaskInfoDAO {
    @Resource
    private DelayTaskInfoMapper delayTaskInfoMapper;

    /**
     * 插入一条数据
     *
     * @param delayTaskInfoDO 实体
     * @return 是否成功
     */
    public boolean insert(DelayTaskInfoDO delayTaskInfoDO) {
        int result = delayTaskInfoMapper.insert(delayTaskInfoDO);
        return result > 0;
    }

    /**
     * 更新
     *
     * @param delayTaskInfoDO 实体
     * @return 是否成功
     */
    public boolean update(DelayTaskInfoDO delayTaskInfoDO) {
        int result = delayTaskInfoMapper.update(delayTaskInfoDO);
        return result > 0;
    }

    /**
     * 删除
     *
     * @param id
     * @return 是否成功
     */
    public boolean delete(Long id) {
        int result = delayTaskInfoMapper.delete(id);
        return result > 0;
    }

    /**
     * getDelayTaskInfoDO
     *
     * @param id
     * @return DelayTaskInfoDO
     */
    public DelayTaskInfoDO get(Long id) {
        DelayTaskInfoDO delayTaskInfo = delayTaskInfoMapper.get(id);
        return delayTaskInfo;
    }

    /**
     * queryByCondition
     *
     * @param delayTaskInfoQuery
     * @return DelayTaskInfoDO
     */
    public List<DelayTaskInfoDO> queryByCondition(DelayTaskInfoQuery delayTaskInfoQuery){
        List<DelayTaskInfoDO> delayTaskInfoDOS = delayTaskInfoMapper.queryByCondition(delayTaskInfoQuery);
        return delayTaskInfoDOS;
    }
}
