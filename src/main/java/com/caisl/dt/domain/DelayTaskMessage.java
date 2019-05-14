package com.caisl.dt.domain;

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
public class DelayTaskMessage implements Delayed {
    private Long delayTaskId;
    private Long triggerTime;

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.triggerTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        DelayTaskMessage delayTaskMessage = (DelayTaskMessage) o;
        return delayTaskId < delayTaskMessage.getDelayTaskId() ? 1 : -1;
    }
}
