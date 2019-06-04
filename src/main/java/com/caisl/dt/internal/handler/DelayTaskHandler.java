package com.caisl.dt.internal.handler;

import ch.qos.logback.classic.Level;
import com.caisl.dt.common.constant.DelayTaskConstant;
import com.caisl.dt.common.constant.DelayTaskStatusEnum;
import com.caisl.dt.common.dao.DelayTaskDAO;
import com.caisl.dt.common.dataobject.DelayTaskDO;
import com.caisl.dt.common.query.DelayTaskQuery;
import com.caisl.dt.domain.DelayTaskMessage;
import com.caisl.dt.internal.queue.DelayTaskQueue;
import com.caisl.dt.mq.producer.DelayTaskMessageProducer;
import com.caisl.dt.system.helper.ShardingItemHelper;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.LogUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * DelayTaskHandler
 *
 * @author caisl
 * @since 2019-05-09
 */
@Component
public class DelayTaskHandler implements IDelayTaskHandler {
    /**
     * 任务循环每页大小
     */
    private final static int TASK_LOOP_PAGE_SIZE = 100;

    /**
     * 任务循环次数最大值
     */
    private final static int MAX_TASK_LOOP_INDEX = 200;

    private final static DelayTaskQueue delayTaskQueue = DelayTaskQueue.INSTANCE;

    @Resource
    private ShardingItemHelper shardingItemHelper;
    @Resource
    private DelayTaskDAO delayTaskDAO;
    @Resource
    private DelayTaskMessageProducer delayTaskMessageProducer;

    @Override
    public boolean loadTask() {
        //当前循环体的集合大小
        int curLoopListSize = TASK_LOOP_PAGE_SIZE;
        Long beginId = 0L;
        int curLoopIndex = 1;
        while (curLoopListSize == TASK_LOOP_PAGE_SIZE && curLoopIndex < MAX_TASK_LOOP_INDEX) {
            LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.JOB, Level.INFO, "开始执行第" + curLoopIndex + "次扫描");
            try {
                DelayTaskQuery delayTaskQuery = buildDelayTaskQuery(beginId);
                //扫描 now - 任务补偿时间（2分钟 ）<= 触发时间 <= now + 任务扫描间隔时间（5分钟）
                List<DelayTaskDO> delayTaskDOS = delayTaskDAO.queryListByQuery(delayTaskQuery);
                if (CollectionUtils.isEmpty(delayTaskDOS)) {
                    break;
                } else {
                    curLoopListSize = delayTaskDOS.size();
                }
                for (DelayTaskDO delayTaskDO : delayTaskDOS) {
                    if (beginId < delayTaskDO.getDelayTaskId()) {
                        beginId = delayTaskDO.getDelayTaskId();
                    }
                    delayTaskQueue.add(DelayTaskMessage.builder().delayTaskId(delayTaskDO.getDelayTaskId()).triggerTime(delayTaskDO.getTriggerTime()).build());
                    delayTaskDO.setStatus(DelayTaskStatusEnum.LOAD.getCode());
                    delayTaskDO.setOpTime(System.currentTimeMillis());
                }
                //批量更新状态
                delayTaskDAO.updateStatusBatch(delayTaskDOS);
            } catch (Exception e) {
                LogUtil.log(DelayTaskLoggerFactory.BUSINESS, DelayTaskLoggerMarker.JOB, Level.ERROR, "loadTask error", e);
            } finally {
                curLoopIndex++;
            }
        }

        return true;
    }

    @Override
    public boolean dealTask(DelayTaskMessage delayTaskMessage) {
        //1.查询数据库中是否存在该任务
        DelayTaskDO delayTaskDO = delayTaskDAO.selectByPrimaryKey(delayTaskMessage.getDelayTaskId());
        if(delayTaskDO == null){
            return false;
        }
        //2.检验任务
        if(delayTaskDO.getStatus() != DelayTaskStatusEnum.LOAD.getCode()){
            return true;
        }
        //任务是否属于该台服务器处理
        if(!shardingItemHelper.getLocalShardingIds().contains(delayTaskDO.getShardingId())){
            //logger
            return true;
        }
        //3.更新任务状态，MVCC乐观锁控制，保证任务不重复处理
        delayTaskDO.setStatus(DelayTaskStatusEnum.SENDING.getCode());
        delayTaskDO.setOpTime(System.currentTimeMillis());
        if(delayTaskDAO.updateStatus(delayTaskDO) > 0) {
            //4.发送MQ消息
            delayTaskMessageProducer.sendMsg(delayTaskDO);
        }
        return false;
    }

    /**
     * buildDelayTaskQuery
     *
     * @param beginId
     * @return
     */
    private DelayTaskQuery buildDelayTaskQuery(Long beginId) {
        return DelayTaskQuery.builder().beginId(beginId).pageSize(MAX_TASK_LOOP_INDEX)
                .triggerTime(System.currentTimeMillis() + DelayTaskConstant.LOAD_JOB_TRIGGER_PERIOD)
                .compensateTime(System.currentTimeMillis() - DelayTaskConstant.DELAY_TASK_COMPENSATE_TIME)
                .shardingIds(shardingItemHelper.getLocalShardingIds()).build();
    }
}
