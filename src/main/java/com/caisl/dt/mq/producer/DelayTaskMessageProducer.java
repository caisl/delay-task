package com.caisl.dt.mq.producer;

import ch.qos.logback.classic.Level;
import com.caisl.dt.common.dataobject.DelayTaskInfoDO;
import com.caisl.dt.system.logger.DelayTaskLoggerFactory;
import com.caisl.dt.system.logger.DelayTaskLoggerMarker;
import com.caisl.dt.system.util.LogUtil;
import com.caisl.dt.system.util.log.KVJsonFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * DelayTaskMessageProducer
 *
 * @author caisl
 * @since 2019-05-16
 */
@Component
public class DelayTaskMessageProducer {

    public String sendMsg(DelayTaskInfoDO delayTaskInfoDO) {
        //组装消息体


        //TODO 具体消息生成由各公司自行封装的MQ框架实现
        String msgId = StringUtils.EMPTY;

        LogUtil.log(DelayTaskLoggerFactory.MQ, DelayTaskLoggerMarker.DELAY_TASK_MSG, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("sendMsg success")
                .add("msgId", msgId)
                .add("topic", delayTaskInfoDO.getTopic())
                .add("tag", delayTaskInfoDO.getTag())
                .add("delayTaskId", delayTaskInfoDO.getId())
                .add("timeLoss", System.currentTimeMillis() - delayTaskInfoDO.getTaskTriggerTime())));
        return msgId;
    }
}
