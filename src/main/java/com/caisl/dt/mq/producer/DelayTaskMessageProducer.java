package com.caisl.dt.mq.producer;

import ch.qos.logback.classic.Level;
import com.caisl.dt.common.dataobject.DelayTaskDO;
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

    public String sendMsg(DelayTaskDO delayTaskDO) {
        String msgId = StringUtils.EMPTY;
        //TODO 具体消息生成由各公司自行封装的MQ框架实现
        LogUtil.log(DelayTaskLoggerFactory.MQ, DelayTaskLoggerMarker.DELAY_TASK_MSG, Level.INFO, LogUtil.formatLog(KVJsonFormat.title("sendMsg")
                .add("topic", delayTaskDO.getTopic())
                .add("tag", delayTaskDO.getTag())
                .add("delayTaskId", delayTaskDO.getDelayTaskId())));
        return msgId;
    }
}
