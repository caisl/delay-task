package com.caisl.dt.service;

import com.alibaba.common.convert.Convert;
import com.caisl.dt.common.constant.DelayTaskStatusEnum;
import com.caisl.dt.common.dao.DelayTaskDAO;
import com.caisl.dt.common.dataobject.DelayTaskDO;
import com.caisl.dt.domain.AddDelayTaskDTO;
import com.caisl.dt.domain.DelayTaskMessage;
import com.caisl.dt.domain.Result;
import com.caisl.dt.common.constant.ResultCodeEnum;
import com.caisl.dt.internal.queue.DelayTaskQueue;
import com.caisl.dt.internal.sharding.ShardingIdSelector;
import com.caisl.dt.system.util.ResultUtil;
import com.caisl.dt.system.util.UniqueIdUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * DelayTaskService
 *
 * @author caisl
 * @since 2019-05-07
 */
@Service
public class DelayTaskService implements IDelayTaskService {

    private DelayTaskQueue delayTaskQueue = DelayTaskQueue.INSTANCE;
    @Resource
    private ShardingIdSelector randomSelector;

    @Resource
    private DelayTaskDAO delayTaskDAO;

    @Override
    public Result<Long> addTask(AddDelayTaskDTO addDelayTaskDTO) {
        //1.参数检查
        checkParams(addDelayTaskDTO);
        DelayTaskDO delayTaskDO;
        //2.任务触发时间是否大于调度任务启动间隔时间
        if (addDelayTaskDTO.getTimeUnit().toMinutes(addDelayTaskDTO.getDelayTime()) <= 5) {
            //直接调度到队列中，任务状态为LOAD
            delayTaskDO = buildDelayTaskDO(addDelayTaskDTO, DelayTaskStatusEnum.INIT, Boolean.TRUE);
            delayTaskQueue.add(buildDelayTaskMessage(delayTaskDO));
        } else {
            //任务状态为INIT
            delayTaskDO = buildDelayTaskDO(addDelayTaskDTO, DelayTaskStatusEnum.INIT, Boolean.FALSE);
        }
        //3.任务持久化
        if (delayTaskDAO.insert(delayTaskDO) <= 0) {
            return ResultUtil.failResult(ResultCodeEnum.INSERT_TASK_FAIL);
        }

        return ResultUtil.successResult(delayTaskDO.getDelayTaskId());
    }

    @Override
    public Result<Boolean> cancelTask(Long taskId) {
        return null;
    }


    /**
     * buildDelayTaskMessage
     *
     * @param delayTaskDO
     * @return
     */
    private DelayTaskMessage buildDelayTaskMessage(DelayTaskDO delayTaskDO) {
        return DelayTaskMessage.builder().delayTaskId(delayTaskDO.getDelayTaskId()).triggerTime
                (delayTaskDO.getTriggerTime()).build();
    }

    /**
     * buildDelayTaskDO
     *
     * @param addDelayTaskDTO
     * @param statusEnum
     * @return
     */
    private DelayTaskDO buildDelayTaskDO(AddDelayTaskDTO addDelayTaskDTO, DelayTaskStatusEnum statusEnum, boolean
                                         isLocalNode) {
        DelayTaskDO delayTaskDO = DelayTaskDO.builder()
                .delayTaskId(UniqueIdUtil.nextId())
                .triggerTime(getTriggerTimeMillis(addDelayTaskDTO.getDelayTime(), addDelayTaskDTO.getTimeUnit()))
                .extendField(StringUtils.EMPTY)
                .params(addDelayTaskDTO.getParamJson())
                .status(statusEnum.getCode())
                .tag(addDelayTaskDTO.getTag())
                .topic(addDelayTaskDTO.getTopic())
                .shardingId(getShardingId(isLocalNode)).build();
        return delayTaskDO;
    }

    /**
     * calculate shardingId
     *
     * @return
     */
    private Integer getShardingId(boolean isLocalNode) {
        return Convert.asInt(randomSelector.select(isLocalNode));
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

    /**
     * 参数检查
     *
     * @param addDelayTaskDTO
     */
    private void checkParams(AddDelayTaskDTO addDelayTaskDTO) {
        if (StringUtils.isBlank(addDelayTaskDTO.getTopic())) {
            throw new RuntimeException("入参topic不能为空");
        }
        if (StringUtils.isBlank(addDelayTaskDTO.getTag())) {
            throw new RuntimeException("入参tag不能为空");
        }
        if (addDelayTaskDTO.getDelayTime() == null) {
            throw new RuntimeException("入参delayTime不能为空");
        }
        if (addDelayTaskDTO.getTimeUnit() == null) {
            throw new RuntimeException("入参timeUnit不能为空");
        }

    }
}
