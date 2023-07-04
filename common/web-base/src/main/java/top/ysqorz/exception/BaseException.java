package top.ysqorz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.util.CommonUtils;
import top.ysqorz.util.I18nUtils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 业务异常
 */
public abstract class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Object[] codeList;

    public BaseException(String msg, Throwable cause, Object... codeList) {
        super(msg, cause);
        this.codeList = codeList;
    }

    public BaseException(Throwable cause, Object... codeList) {
        this(null, cause, codeList);
    }

    /**
     * 实际抛出异常(创建异常对象)时，动态传入的业务错误码。
     * 该集合中的元素是可以是StatusCode或者String，该集合的元素会被翻译并拼接起来作为当前业务异常的国际化错误信息
     */
    public BaseException(Object[] codeList) {
        this(null, codeList);
    }

    /**
     * 当前业务异常类默认的业务状态码，比较模糊。与异常的实例无关，与异常类有关
     */
    public abstract StatusCode getDefaultCode();

    /**
     * 当前业务异常对应的http状态码
     */
    public abstract HttpStatus getHttpStatus();

    private StatusCode getStatusCode() {
        if (ObjectUtils.isEmpty(codeList)) {
            return getDefaultCode();
        }
        List<StatusCode> codes = Arrays.stream(codeList)
                .filter(code -> code instanceof StatusCode)
                .map(code -> (StatusCode) code)
                .collect(Collectors.toList());
        if (codes.size() > 1) {
            return getDefaultCode();
        }
        return codes.get(0);
    }

    public Integer getIntegerCode() {
        return getStatusCode().getCode();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    private String getTranslatedMsgFromCode() {
        if (ObjectUtils.isEmpty(codeList)) {
            codeList = new Object[]{getDefaultCode()};
        }
        return I18nUtils.translateMsg(codeList);
    }

    @Override
    public String getMessage() {
        return CommonUtils.joinStr(System.lineSeparator(), getTranslatedMsgFromCode(), super.getMessage());
    }

    @Override
    public void printStackTrace() {
        this.printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        super.printStackTrace(printStream);
        if (!Objects.isNull(printStream)) {
            getCause().printStackTrace(printStream);
        }
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        super.printStackTrace(printWriter);
        if (!Objects.isNull(getCause())) {
            getCause().printStackTrace(printWriter);
        }
    }
}
