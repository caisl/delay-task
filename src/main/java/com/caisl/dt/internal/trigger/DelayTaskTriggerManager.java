package com.caisl.dt.internal.trigger;


import ch.qos.logback.classic.Level;
import com.caisl.dt.common.constant.*;
import com.caisl.dt.common.dao.DelayTaskClockDAO;
import com.caisl.dt.common.dao.DelayTaskInfoDAO;
import com.caisl.dt.common.dataobject.DelayTaskClockDO;
import com.caisl.dt.common.dataobject.DelayTaskInfoDO;
import com.caisl.dt.common.query.DelayTaskClockQuery;
import com.caisl.dt.common.query.DelayTaskInfoQuery;
import com.caisl.dt.domain.DelayTaskDTO;
import com.caisl.dt.internal.redis.RedisCacheService;
import com.caisl.dt.mq.producer.DelayTaskMessageProducer;
import com.caisl.dt.system.exception.BizException;
import com.caisl.dt.system.helper.ShardingItemHelper;
import com.caisl.dt.system.helper.UniqueIdHelper;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.LogUtil;
import com.caisl.dt.system.util.log.KVJsonFormat;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DelayTaskTriggerManager
 *
 * @author caisl
 * @since 2021/3/02
 */
@Component
public class DelayTaskTriggerManager {

    private DelayTaskClockWheelTimer delayTaskClockWheelTimer = DelayTaskClockWheelTimer.INSTANCE;
    @Resource
    private DelayTaskInfoDAO delayTaskInfoDAO;
    @Resource
    private DelayTaskClockDAO delayTaskClockDAO;
    @Resource
    private DelayTaskMessageProducer delayTaskMessageProducer;
    @Resource
    private RedisCacheService redisCacheService;
    @Resource
    private UniqueIdHelper uniqueIdHelper;
    @Resource
    private ShardingItemHelper shardingItemHelper;

    public Integer compensateClock(List<Integer> clockStatusList) {
        //1.考虑现在的业务量，暂时不用分页查询
        Long now = this.transToSecondGranularity(System.currentTimeMillis());
        DelayTaskClockQuery delayTaskClockQuery = DelayTaskClockQuery.builder().taskTriggerTime(now - DelayTaskConstant.TASK_CLOCK_GRANULARITY_1_SECOND)
                .compensateTime(now - DelayTaskConstant.DELAY_TASK_COMPENSATE_TIME).clockStatusList(clockStatusList).build();
        List<DelayTaskClockDO> delayTaskClockDOS = delayTaskClockDAO.queryListByQuery(delayTaskClockQuery);
        if (CollectionUtils.isEmpty(delayTaskClockDOS)) {
            return 0;
        }
        for (DelayTaskClockDO delayTaskClockDO : delayTaskClockDOS) {
            if (delayTaskClockDO.getTaskTriggerTime() <= now) {
                this.dealTask(delayTaskClockDO.getTaskTriggerTime(), DelayTaskClockStatusEnum.EXPIRE_EXECUTE);
            }
        }
        if (delayTaskClockDOS.size() > 0) {
            LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_CLOCK, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("任务补偿执行结束")
                    .add("size", delayTaskClockDOS.size())));
        }
        return delayTaskClockDOS.size();
    }

    public Integer loadClock(List<Integer> clockStatusList) {
        //当前循环体的集合大小
        int curLoopListSize = DelayTaskConstant.TASK_LOOP_PAGE_SIZE;
        Long beginId = 0L;
        int loadClockNum = 0;
        int curLoopIndex = 1;
        while (curLoopListSize == DelayTaskConstant.TASK_LOOP_PAGE_SIZE && curLoopIndex < DelayTaskConstant.MAX_TASK_LOOP_INDEX) {
            try {
                DelayTaskClockQuery delayTaskClockQuery = this.buildDelayTaskClockQuery(beginId, clockStatusList);
                //扫描 now - 任务补偿时间（5分钟 ）<= 触发时间 <= now + 任务扫描间隔时间（5分钟）
                List<DelayTaskClockDO> delayTaskClockDOS = delayTaskClockDAO.queryListByQuery(delayTaskClockQuery);
                if (CollectionUtils.isEmpty(delayTaskClockDOS)) {
                    break;
                } else {
                    curLoopListSize = delayTaskClockDOS.size();
                    loadClockNum += curLoopListSize;
                }
                LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_CLOCK, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("开始加载时钟到时间轮")
                        .add("msg", "执行第" + curLoopIndex + "次扫描")
                        .add("curLoopListSize", curLoopListSize)));
                List<DelayTaskClockDO> loadClocks = new ArrayList<>(delayTaskClockDOS.size());
                for (DelayTaskClockDO delayTaskClockDO : delayTaskClockDOS) {
                    if (beginId < delayTaskClockDO.getId()) {
                        beginId = delayTaskClockDO.getId();
                    }
                    //如果任务时钟小于当前时间，说明是补偿的任务，需要立即触发
                    if (delayTaskClockDO.getTaskTriggerTime() <= System.currentTimeMillis()) {
                        this.dealTask(delayTaskClockDO.getTaskTriggerTime(), DelayTaskClockStatusEnum.EXPIRE_EXECUTE);
                    } else {
                        this.loadTaskIntoWheelTimer(delayTaskClockDO.getTaskTriggerTime());
                    }
                    delayTaskClockDO.setClockStatus(DelayTaskClockStatusEnum.LOAD.getStatus());
                    loadClocks.add(delayTaskClockDO);
                }
                //批量更新状态
                if(loadClocks.size() >0) {
                    delayTaskClockDAO.updateStatusBatch(loadClocks);
                }
            } catch (Exception e) {
                LogUtil.log(DelayTaskLoggerFactory.EXCEPTION_HANDLER, DelayTaskLoggerMarker.EXCEPTION_HANDLER, Level.ERROR, "loadTask error", e);
            } finally {
                curLoopIndex++;
            }
        }
        if (loadClockNum > 0) {
            LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_CLOCK, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("加载时钟到时间轮结束").add("loadClockNum", loadClockNum)));
        }
        return loadClockNum;
    }

    @Transactional(rollbackFor = Exception.class)
    @Retryable(value = SQLIntegrityConstraintViolationException.class,maxAttempts = 3,backoff = @Backoff(delay = 10,multiplier = 1.5))
    public Long addTask(Long taskTriggerTime, DelayTaskDTO delayTaskDTO) {
        //1.参数检查
        this.checkParams(delayTaskDTO);

        taskTriggerTime = this.transToSecondGranularity(taskTriggerTime);
        //2.查询对应的任务时钟
        DelayTaskClockDO delayTaskClockDO = delayTaskClockDAO.queryByTaskTriggerTime(taskTriggerTime);
        //3.任务时钟未存在，需要持久化任务时钟
        if (delayTaskClockDO == null) {
            //3.1 任务触发时间是否大于调度任务启动间隔时间
            if (taskTriggerTime - System.currentTimeMillis() < DelayTaskConstant.LOAD_JOB_TRIGGER_PERIOD) {
                //添加到时间轮里面，等待调度
                this.loadTaskIntoWheelTimer(taskTriggerTime);
                delayTaskClockDO = this.buildDelayTaskClockDO(taskTriggerTime, DelayTaskClockStatusEnum.LOAD);
            } else {
                delayTaskClockDO = this.buildDelayTaskClockDO(taskTriggerTime, DelayTaskClockStatusEnum.PREPARE);
            }
            if (!delayTaskClockDAO.insert(delayTaskClockDO)) {
                throw new BizException(ResultCodeEnum.INSERT_TASK_FAIL.getCode(), ResultCodeEnum.INSERT_TASK_FAIL.getMessage());
            }
        }

        DelayTaskInfoDO delayTaskInfoDO = buildDelayTaskInfoDO(delayTaskDTO, DelayTaskStatusEnum.INIT, taskTriggerTime);
        //4.延迟任务持久化
        if (!delayTaskInfoDAO.insert(delayTaskInfoDO)) {
            throw new BizException(ResultCodeEnum.INSERT_TASK_FAIL.getCode(), ResultCodeEnum.INSERT_TASK_FAIL.getMessage());
        }
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_OPERATE, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("addTask success")
                .add("delayTaskId", delayTaskInfoDO.getId())
                .add("taskTriggerTime", delayTaskInfoDO.getTaskTriggerTime())));
        return delayTaskInfoDO.getId();
    }

    @Recover
    public Long recover(Exception e) throws Exception {
        LogUtil.log(DelayTaskLoggerFactory.EXCEPTION_HANDLER, DelayTaskLoggerMarker.EXCEPTION_HANDLER, Level.ERROR, "addTask fail", e);
        //记日志到数据库
        throw e;
    }

    private Long transToSecondGranularity(Long triggerTime) {
        long remainder = triggerTime % DelayTaskConstant.TASK_CLOCK_GRANULARITY_1_SECOND;
        if (remainder == 0) {
            return triggerTime;
        }
        return triggerTime + DelayTaskConstant.TASK_CLOCK_GRANULARITY_1_SECOND - remainder;
    }

    /**
     * buildDelayTaskInfoDO
     *
     * @param delayTaskDTO
     * @param statusEnum
     * @return
     */
    private DelayTaskInfoDO buildDelayTaskInfoDO(DelayTaskDTO delayTaskDTO, DelayTaskStatusEnum statusEnum, Long triggerTime) {
        DelayTaskInfoDO delayTaskInfoDO = new DelayTaskInfoDO();
        delayTaskInfoDO.setId(uniqueIdHelper.nextId());
        delayTaskInfoDO.setTaskTriggerTime(triggerTime);
        delayTaskInfoDO.setExtendField(StringUtils.EMPTY);
        delayTaskInfoDO.setParams(delayTaskDTO.getParamJson());
        delayTaskInfoDO.setTaskStatus(statusEnum.getStatus());
        delayTaskInfoDO.setTag(delayTaskDTO.getTag());
        delayTaskInfoDO.setTopic(delayTaskDTO.getTopic());
        delayTaskInfoDO.setAppName(delayTaskDTO.getAppName());
        delayTaskInfoDO.setMsgId(StringUtils.EMPTY);
        delayTaskInfoDO.setProducerGroupId(delayTaskDTO.getProducerGroupId());
        return delayTaskInfoDO;
    }

    /**
     * buildDelayTaskClockDO
     *
     * @param triggerTime
     * @param clockStatusEnum
     * @return
     */
    private DelayTaskClockDO buildDelayTaskClockDO(Long triggerTime, DelayTaskClockStatusEnum clockStatusEnum) {
        DelayTaskClockDO delayTaskClockDO = new DelayTaskClockDO();
        delayTaskClockDO.setId(uniqueIdHelper.nextId());
        delayTaskClockDO.setTaskTriggerTime(triggerTime);
        delayTaskClockDO.setClockStatus(clockStatusEnum.getStatus());
        return delayTaskClockDO;
    }

    /**
     * 参数检查
     *
     * @param delayTaskDTO
     */
    private void checkParams(DelayTaskDTO delayTaskDTO) {
        if (StringUtils.isBlank(delayTaskDTO.getTopic())) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR.getCode(), "入参topic不能为空");
        }
        if (StringUtils.isBlank(delayTaskDTO.getProducerGroupId())) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR.getCode(), "入参producerGroupId不能为空");
        }
        if (!delayTaskDTO.getProducerGroupId().equals("GID_" + delayTaskDTO.getTopic())) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR.getCode(), "GroupID必须是GID_topic格式");
        }
        if (StringUtils.isBlank(delayTaskDTO.getTag())) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR.getCode(), "入参tag不能为空");
        }
    }

    /**
     * buildDelayTaskQuery
     *
     * @param beginId
     * @return
     */
    private DelayTaskClockQuery buildDelayTaskClockQuery(Long beginId, List<Integer> clockStatusList) {
        return DelayTaskClockQuery.builder().beginId(beginId).pageSize(DelayTaskConstant.MAX_TASK_LOOP_INDEX)
                .taskTriggerTime(System.currentTimeMillis() + DelayTaskConstant.LOAD_JOB_TRIGGER_PERIOD)
                .compensateTime(System.currentTimeMillis() - DelayTaskConstant.DELAY_TASK_COMPENSATE_TIME)
                .clockStatusList(clockStatusList).build();
    }


    private String createDealTaskLockKey(Long delayTaskId) {
        return new StringBuilder("DEAL_TASK_LOCK_KEY:").append(delayTaskId).toString();
    }

    /**
     * 往时间轮里面添加任务
     *
     * @param clockTriggerTime
     * @return
     */
    public boolean loadTaskIntoWheelTimer(Long clockTriggerTime) {
        Long now = System.currentTimeMillis();
        if (clockTriggerTime < System.currentTimeMillis()) {
            LogUtil.log(DelayTaskLoggerFactory.EXCEPTION_HANDLER, DelayTaskLoggerMarker.DELAY_TASK_CLOCK, Level.ERROR, LogUtil.formatLog(KVJsonFormat.title("处理loadClock错误")
                    .add("error", "this job has expire, clockTriggerTime:" + clockTriggerTime + ",currentTime:" + now)));
            return false;
        }
        delayTaskClockWheelTimer.getWheelTimer().newTimeout(new DelayTaskClockWheelTimerTask(clockTriggerTime), clockTriggerTime - now, TimeUnit.MILLISECONDS);
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_CLOCK, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("loadTaskIntoWheelTimer")
                .add("msg", "load clock, clockTriggerTime:" + clockTriggerTime)
                .add("currentPendingTimeouts", delayTaskClockWheelTimer.getWheelTimer().pendingTimeouts())));

        return true;
    }

    /**
     * 时间轮触发执行方法
     * 加载时钟下面的任务列表，批量发送mq消息
     *
     * @param taskTriggerTime
     */
    public void dealTask(Long taskTriggerTime, DelayTaskClockStatusEnum taskClockStatusEnum) {
        DelayTaskClockDO delayTaskClockDO = delayTaskClockDAO.queryByTaskTriggerTime(taskTriggerTime);
        if (delayTaskClockDO == null) {
            return;
        }
        LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_OPERATE, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("开始处理时间轮任务")
                .add("shardingIndex", shardingItemHelper.getIndex()).add("taskTriggerTime", taskTriggerTime)));
        try {
            //更新时钟状态
            delayTaskClockDO.setClockStatus(taskClockStatusEnum.getStatus());
            delayTaskClockDAO.update(delayTaskClockDO);
            //查询该任务时钟是否有任务需要执行 前期业务量比较少，先全部查询处理，后续业务量增加再考虑分批次
            List<DelayTaskInfoDO> delayTaskInfoDOS = delayTaskInfoDAO.queryByCondition(DelayTaskInfoQuery.builder().taskTriggerTime(taskTriggerTime).taskStatus(DelayTaskStatusEnum.INIT.getStatus()).index(shardingItemHelper.getIndex()).total(shardingItemHelper.getIndexTotal()).build());
            if (CollectionUtils.isEmpty(delayTaskInfoDOS)) {
                return;
            }
            if (delayTaskInfoDOS.size() > 100) {
                LogUtil.log(DelayTaskLoggerFactory.EXCEPTION_HANDLER, DelayTaskLoggerMarker.DELAY_TASK_OPERATE, Level.ERROR, LogUtil.formatLog(KVJsonFormat.title("延迟消息批量处理过多")
                        .add("taskTriggerTime", taskTriggerTime)
                        .add("size", delayTaskInfoDOS.size())));
            }
            Long now = System.currentTimeMillis();
            for (DelayTaskInfoDO delayTaskInfoDO : delayTaskInfoDOS) {
                //任务时钟加锁，保证任务不会被重复调度
                String lockKey = createDealTaskLockKey(delayTaskInfoDO.getId());
                if (!redisCacheService.setnx(lockKey, CacheExpireTime.EXPIRE_MINUTE, now.toString())) {
                    return;
                }
                try {
                    delayTaskInfoDO.setMsgId(delayTaskMessageProducer.sendMsg(delayTaskInfoDO));
                    delayTaskInfoDO.setTaskStatus(DelayTaskStatusEnum.SEND.getStatus());
                    delayTaskInfoDAO.update(delayTaskInfoDO);
                } finally {
                    if (now.toString().equals(redisCacheService.get(lockKey))) {
                        redisCacheService.del(lockKey);
                    }
                }
            }
            LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.DELAY_TASK_OPERATE, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("延迟任务执行成功")
                    .add("taskTriggerTime", taskTriggerTime).add("delayTaskSize", delayTaskInfoDOS.size())
                    .add("currentPendingTimeouts", delayTaskClockWheelTimer.getWheelTimer().pendingTimeouts())));
        } catch (Exception e) {
            LogUtil.log(DelayTaskLoggerFactory.EXCEPTION_HANDLER, DelayTaskLoggerMarker.EXCEPTION_HANDLER, Level.ERROR, LogUtil.formatLog(KVJsonFormat.title("发送延迟消息失败")
                    .add("delayTaskClockDO", delayTaskClockDO)), e);
        }
    }


    private class DelayTaskClockWheelTimerTask implements TimerTask {
        private Long triggerTime;

        DelayTaskClockWheelTimerTask(Long triggerTime) {
            this.triggerTime = triggerTime;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            dealTask(triggerTime, DelayTaskClockStatusEnum.EXECUTE);
        }
    }

}
