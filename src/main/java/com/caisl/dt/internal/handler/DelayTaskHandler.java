package com.caisl.dt.internal.handler;

import ch.qos.logback.classic.Level;
import com.caisl.dt.common.constant.DelayTaskConstant;
import com.caisl.dt.common.dao.DelayTaskDAO;
import com.caisl.dt.common.dataobject.DelayTaskDO;
import com.caisl.dt.common.query.DelayTaskQuery;
import com.caisl.dt.domain.DelayTaskMessage;
import com.caisl.dt.internal.queue.DelayTaskQueue;
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
                }
            } catch (Exception e) {
                //todo logger
            } finally {
                curLoopIndex++;

            }
        }

        return true;
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
