package com.caisl.dt.common.constant;

/**
 * DelayTaskClockStatusEnum
 *
 * @author caisl
 * @since 2021/3/02
 */
public enum DelayTaskClockStatusEnum {
    PREPARE(1, "初始化"),
    LOAD(2, "任务已加载"),
    EXECUTE(3, "已执行"),
    EXPIRE_EXECUTE(4, "过期执行");
    private Integer status;
    private String desc;

    DelayTaskClockStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static DelayTaskClockStatusEnum getByStatus(Integer status) {
        for (DelayTaskClockStatusEnum taskStatusEnums : values()) {
            if (taskStatusEnums.getStatus().equals(status)) {
                return taskStatusEnums;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
