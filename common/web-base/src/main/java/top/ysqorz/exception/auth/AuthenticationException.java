package top.ysqorz.exception.auth;

import org.springframework.http.HttpStatus;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.exception.BaseException;

/**
 * 用户未登录认证或者登录凭证失效非法的异常，http状态码为401
 */
public class AuthenticationException extends BaseException {
    public AuthenticationException(Object... codeList) {
        super(codeList);
    }

    @Override
    public StatusCode getDefaultCode() {
        return BaseStatusCode.NO_AUTHENTICATION;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
