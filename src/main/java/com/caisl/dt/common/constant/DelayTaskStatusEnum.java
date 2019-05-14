package com.caisl.dt.common.constant;

/**
 * DelayTaskStatusEnum
 *
 * @author caisl
 * @since 2019-04-29
 */
public enum DelayTaskStatusEnum {
    INIT(1, "初始化"),
    LOAD(2, "任务已加载"),
    SENDING(3, "消息已发放"),
    SUCCESS(4, "业务处理成功"),
    FAIL(5, "业务处理失败"),
    CANCEL(6, "业务取消");
    private Integer code;
    private String desc;

    DelayTaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
