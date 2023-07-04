package top.ysqorz.exception.business;

import org.springframework.http.HttpStatus;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.exception.BaseException;

/**
 * 可预见的正常业务失败的异常，http状态密码仍然为200，如：密码错误
 */
public class ServiceFailedException extends BaseException {
    public ServiceFailedException(Object... codeList) {
        super(codeList);
    }

    @Override
    public StatusCode getDefaultCode() {
        return BaseStatusCode.BUSINESS_FAILED;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.OK;
    }
}
