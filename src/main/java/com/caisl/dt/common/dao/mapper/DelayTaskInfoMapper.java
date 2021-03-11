package com.caisl.dt.common.dao.mapper;

import com.caisl.dt.common.dataobject.DelayTaskInfoDO;
import com.caisl.dt.common.query.DelayTaskInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * DelayTaskInfoMapper
 *
 * @author caisl
 * @since 2021/3/02
 */
@Mapper
public interface DelayTaskInfoMapper {

    /**
     * DelayTaskInfo - 添加
     *
     * @param delayTaskInfoDO 实体
     * @return 添加结果
     */
    int insert(DelayTaskInfoDO delayTaskInfoDO);

    /**
     * DelayTaskInfo - 修改
     *
     * @param delayTaskInfoDO 实体
     * @return 修改结果
     */
    int update(DelayTaskInfoDO delayTaskInfoDO);

    /**
     * DelayTaskInfo - 删除
     *
     * @param id
     * @return 删除结果
     */
    int delete(Long id);

    /**
     * 根据PK查询
     *
     * @return DelayTaskInfoDO
     */
    DelayTaskInfoDO get(Long id);

    /**
     * queryByCondition
     *
     * @param delayTaskInfoQuery
     * @return DelayTaskInfoDO
     */
    List<DelayTaskInfoDO> queryByCondition(DelayTaskInfoQuery delayTaskInfoQuery);
}
