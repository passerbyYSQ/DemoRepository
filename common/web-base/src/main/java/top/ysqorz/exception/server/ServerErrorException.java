package top.ysqorz.exception.server;

import org.springframework.http.HttpStatus;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.exception.BaseException;

public class ServerErrorException extends BaseException {
    /**
     * 将异常包装成ServerErrorException
     */
    public ServerErrorException(Throwable throwable) {
        super(throwable.getMessage(), throwable.getCause());
    }

    /**
     * 500异常可能存在无法翻译的信息，可通过该构造方法传入
     */
    public ServerErrorException(String msg) {
        super(msg, null);
    }

    public ServerErrorException(Object... codeList) {
        super(codeList);
    }

    @Override
    public StatusCode getDefaultCode() {
        return BaseStatusCode.SERVER_ERROR;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
