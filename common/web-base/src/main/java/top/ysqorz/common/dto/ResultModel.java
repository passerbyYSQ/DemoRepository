package top.ysqorz.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应结果封装
 */
@Data
@NoArgsConstructor
public class ResultModel<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private T data;
    /**
     * 国际化翻译之后的信息
     */
    private String msg;
    private final LocalDateTime time = LocalDateTime.now();

    private ResultModel(Integer code, T data, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private ResultModel(Integer code, String msg) {
        this(code, null, msg);
    }

    private ResultModel(StatusCode code, T data, Object... args) {
        this(code.getCode(), data, code.getTranslatedMsg(args));
    }

    public static <T> ResultModel<T> wrap(StatusCode code, T data, Object... args) {
        return new ResultModel<>(code, data, args);
    }

    public static <T> ResultModel<T> success(T data) {
        return wrap(BaseStatusCode.SUCCESS, data);
    }

    /**
     * 不建议使用该构造方法，因为强烈不建议向ResultModel直接传入msg
     * 如果使用该构造方法，请确保translatedMsg参数已经被国际化了
     */
    public static <T> ResultModel<T> success(T data, String translatedMsg) {
        return new ResultModel<>(BaseStatusCode.SUCCESS.getCode(), data, translatedMsg);
    }

    public static <T> ResultModel<T> success() {
        return success(null);
    }

    public static <T> ResultModel<T> failure(StatusCode code) {
        return wrap(code, null);
    }

    /**
     * 不建议使用该构造方法，因为强烈不建议向ResultModel直接传入msg
     * 如果使用该构造方法，请确保translatedMsg参数已经被国际化了
     * 在业务层如果因为业务失败想要向前端返回错误信息时，强烈不不建议使用该构造方法，
     * 而应该使用传入StatusCode的构造方法或者抛出对应的业务异常，这样才能确保错误信息被国际化
     */
    public static <T> ResultModel<T> failure(Integer code, String translatedMsg) {
        return new ResultModel<>(code, translatedMsg);
    }
}
