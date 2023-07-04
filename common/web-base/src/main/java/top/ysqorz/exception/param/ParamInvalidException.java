package top.ysqorz.exception.param;

import org.springframework.http.HttpStatus;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.exception.BaseException;

/**
 * 参数相关的异常，http状态码为400，通常为前端传参错误，与实际用户行为无关
 */
public class ParamInvalidException extends BaseException {
    /**
     * 具体的参数错误，比如：StatusCode.TOKEN_IS_NULL
     */
    public ParamInvalidException(Object... codeList) {
        super(codeList);
    }

    /**
     * Validator框架拼接出来的动态的参数错误信息
     */
    public ParamInvalidException(String msg) {
        super(msg, null);
    }

    @Override
    public StatusCode getDefaultCode() {
        return BaseStatusCode.PARAM_INVALID;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
