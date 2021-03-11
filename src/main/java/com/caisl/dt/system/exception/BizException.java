package com.caisl.dt.system.exception;

/**
 * BizException
 *
 * @author shinan
 * @since 2021/3/2
 */
public class BizException extends RuntimeException{
    private static final long serialVersionUID = 8056580241015398148L;
    /**
     * 异常业务编码
     */
    private String code;

    /**
     * 默认异常构造器.
     */
    public BizException() {
        super();
    }

    /**
     * 根据异常信息和原生异常构造对象.
     *
     * @param code    错误码
     * @param msg 异常信息.
     * @param cause   原生异常.
     */
    public BizException(final String code, final String msg, final Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    /**
     * 根据异常构造业务对象，设置 编码及 消息
     *
     * @param code
     * @param msg
     * @author zxh 槟榔
     */
    public BizException(final String code, final String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * 根据异常信息和原生异常构造对象.
     *
     * @param msg 异常信息.
     * @param cause   原生异常.
     */
    public BizException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 根据异常信息构造对象.
     *
     * @param msg 异常信息.
     */
    public BizException(final String msg) {
        super(msg);
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 根据原生异常构造对象.
     *
     * @param cause 原生异常.
     */
    public BizException(final Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
