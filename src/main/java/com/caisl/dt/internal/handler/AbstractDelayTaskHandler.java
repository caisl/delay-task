package com.caisl.dt.internal.handler;

/**
 * AbstractDelayTaskHandler
 *
 * @author caisl
 * @since 2019-05-09
 */
public abstract class AbstractDelayTaskHandler implements IDelayTaskHandler {
    @Override
    public boolean loadTask() {
        return true;
    }

    void queryFromDB(){

    }

    abstract void addTask();
}
