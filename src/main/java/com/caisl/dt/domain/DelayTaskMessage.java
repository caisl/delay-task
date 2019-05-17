package com.caisl.dt.domain;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayTaskMessage
 *
 * @author caisl
 * @since 2019-05-09
 */
@Data
@Builder
public class DelayTaskMessage implements Delayed {
    /**
     * 任务ID
     */
    private Long delayTaskId;
    /**
     * 触发时间
     */
    private Long triggerTime;

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.triggerTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        DelayTaskMessage delayTaskMessage = (DelayTaskMessage) o;
        return delayTaskId < delayTaskMessage.getDelayTaskId() ? -1 : 1;
    }
}
