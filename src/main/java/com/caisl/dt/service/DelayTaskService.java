package com.caisl.dt.service;

import com.caisl.dt.common.constant.DelayTaskStatusEnum;
import com.caisl.dt.common.constant.ResultCodeEnum;
import com.caisl.dt.common.dao.DelayTaskInfoDAO;
import com.caisl.dt.common.dataobject.DelayTaskInfoDO;
import com.caisl.dt.domain.DelayTaskDTO;
import com.caisl.dt.domain.Result;
import com.caisl.dt.internal.sharding.ShardingIdSelector;
import com.caisl.dt.internal.trigger.DelayTaskTriggerManager;
import com.caisl.dt.system.util.ResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * DelayTaskService
 *
 * @author caisl
 * @since 2019-05-07
 */
@Service
public class DelayTaskService implements IDelayTaskService {

    @Resource
    private ShardingIdSelector randomSelector;
    @Resource
    private DelayTaskTriggerManager delayTaskTriggerManager;
    @Resource
    private DelayTaskInfoDAO delayTaskInfoDAO;

    @Override
    public Result<Long> addTaskDelayDynamicTime(Long delayTime, TimeUnit timeUnit, DelayTaskDTO delayTaskDTO) {
        //1.参数检查
        if (delayTime == null) {
            return ResultUtil.failResult(ResultCodeEnum.PARAM_ERROR.getCode(), "入参delayTime不能为空");
        }
        if (timeUnit == null) {
            return ResultUtil.failResult(ResultCodeEnum.PARAM_ERROR.getCode(), "入参timeUnit不能为空");
        }
        return ResultUtil.successResult(delayTaskTriggerManager.addTask(this.getTriggerTimeMillis(delayTime, timeUnit), delayTaskDTO));
    }

    @Override
    public Result<Long> addTaskAbsoluteTime(Long absoluteTime, DelayTaskDTO delayTaskDTO) {
        //1.参数检查
        if (absoluteTime == null) {
            return ResultUtil.failResult(ResultCodeEnum.PARAM_ERROR.getCode(), "入参absoluteTime不能为空");
        }
        if(absoluteTime <= System.currentTimeMillis()){
            return ResultUtil.failResult(ResultCodeEnum.PARAM_ERROR.getCode(), "延迟任务已经过期");
        }
        return ResultUtil.successResult(delayTaskTriggerManager.addTask(absoluteTime, delayTaskDTO));
    }

    @Override
    public Result<Boolean> cancelTask(Long delayTaskId) {
        DelayTaskInfoDO delayTaskInfoDO = delayTaskInfoDAO.get(delayTaskId);
        if (delayTaskInfoDO == null) {
            return ResultUtil.failResult(ResultCodeEnum.TASK_NOT_FIND);
        }
        DelayTaskStatusEnum taskStatusEnum = DelayTaskStatusEnum.getByStatus(delayTaskInfoDO.getTaskStatus());
        switch (taskStatusEnum) {
            case INIT:
                delayTaskInfoDO.setTaskStatus(DelayTaskStatusEnum.CANCEL.getStatus());
                delayTaskInfoDAO.update(delayTaskInfoDO);
                break;
            case SEND:
            case SUCCESS:
            case FAIL:
                return ResultUtil.failResult(ResultCodeEnum.DELAY_TASK_HAS_EXECUTE);
            case CANCEL:
                break;
            default:
                return ResultUtil.failResult(ResultCodeEnum.DELAY_TASK_STATUS_ERROR);
        }
        return ResultUtil.successResult(true);
    }

    @Override
    public Result<Boolean> handleResultNotify(Long delayTaskId, Boolean isSuccess) {
        DelayTaskInfoDO delayTaskInfoDO = delayTaskInfoDAO.get(delayTaskId);
        if(delayTaskInfoDO == null){
            return ResultUtil.failResult(ResultCodeEnum.TASK_NOT_FIND);
        }
        if(isSuccess){
            delayTaskInfoDO.setTaskStatus(DelayTaskStatusEnum.SUCCESS.getStatus());
        }else{
            delayTaskInfoDO.setTaskStatus(DelayTaskStatusEnum.FAIL.getStatus());
        }
        return ResultUtil.successResult(delayTaskInfoDAO.update(delayTaskInfoDO));
    }

    /**
     * calculate shardingId
     *
     * @return
     */
    private Integer getShardingId(boolean isLocalNode) {
        return Optional.ofNullable(randomSelector.select(isLocalNode)).orElse(0);
    }

    /**
     * 计算任务触发时间戳
     *
     * @param delayTime
     * @param timeUnit
     * @return
     */
    private Long getTriggerTimeMillis(Long delayTime, TimeUnit timeUnit) {
        return timeUnit.toMillis(delayTime) + System.currentTimeMillis();
    }
}
