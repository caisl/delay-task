package com.caisl.dt.system.util;


import com.caisl.dt.domain.Result;
import com.caisl.dt.common.constant.ResultCodeEnum;

/**
 * ResultUtil
 *
 * @author caisl
 * @since 2019-01-22
 */
public class ResultUtil {


    public static Result defaultResult() {
        return new Result();
    }


    public static Result defaultFailResult() {
        Result result = defaultResult();
        result.setSuccess(Boolean.FALSE);
        return result;
    }

    public static Result successResult(Object model) {
        Result result = defaultResult();
        result.setModel(model);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    public static Result failResult(String errorCode, String errorMessage) {
        Result result = defaultFailResult();
        result.setResultCode(errorCode);
        result.setMessage(errorMessage);
        return result;
    }

    public static Result failResult(ResultCodeEnum codeEnum) {
        Result result = defaultFailResult();
        result.setResultCode(codeEnum.getCode());
        result.setMessage(codeEnum.getMessage());
        return result;
    }


    public static boolean isResultSuccess(Result result) {
        return null != result && result.isSuccess();
    }

}
