package top.ysqorz.exception.auth;

import org.springframework.http.HttpStatus;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.exception.BaseException;

/**
 * 无权限操作的异常，http状态码为403
 */
public class AuthorizationException extends BaseException {
    public AuthorizationException(Object... codeList) {
        super(codeList);
    }

    @Override
    public StatusCode getDefaultCode() {
        return BaseStatusCode.NO_AUTHORIZATION;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
