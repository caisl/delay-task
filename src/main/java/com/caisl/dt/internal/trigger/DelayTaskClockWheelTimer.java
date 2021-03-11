package com.caisl.dt.internal.trigger;

import com.caisl.dt.internal.factory.DelayTaskThreadFactory;
import io.netty.util.HashedWheelTimer;

import java.util.concurrent.TimeUnit;

/**
 * DelayTaskClockWheelTimer
 *
 * @author caisl
 * @since 2021/3/02
 */
public enum DelayTaskClockWheelTimer {
    INSTANCE;

    private HashedWheelTimer wheelTimer;


    /**
     * 构造一个时间轮，64格 秒级触发精度
     *
     */
    DelayTaskClockWheelTimer(){
        wheelTimer = new HashedWheelTimer(
                new DelayTaskThreadFactory(),
                1000,
                TimeUnit.MILLISECONDS, 64);
    }

    public HashedWheelTimer getWheelTimer() {
        return wheelTimer;
    }
}
