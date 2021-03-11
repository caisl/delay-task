package com.caisl.dt.common.constant;

/**
 * ResultCodeEnum
 *
 * @author caisl
 * @since 2019-05-14
 */
public enum ResultCodeEnum {
    REQUEST_SUCCESS("1", "request success"),
    REQUEST_FAIL("0", "request fail"),

    SYSTEM_ERROR("10000", "系统内部异常"),
    PARAM_ERROR("10001", "参数不合法"),
    RULE_PARAM_ERROR("10002", "规则参数不合法"),


    INSERT_TASK_FAIL("20001", "插入任务失败"),
    TASK_NOT_FIND("20002", "延迟任务不存在"),
    DELAY_TASK_STATUS_ERROR("20003", "延迟任务状态异常"),
    DELAY_TASK_HAS_EXECUTE("20004", "延迟任务已经执行"),

    ;
    private String code;
    private String message;

    ResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
