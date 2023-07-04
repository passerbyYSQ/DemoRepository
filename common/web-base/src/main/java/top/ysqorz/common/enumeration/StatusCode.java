package top.ysqorz.common.enumeration;

import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.util.I18nUtils;

import java.util.Objects;

/**
 * 常量状态码，该对象的引用和内容都不允许在初始化之后修改
 */
@Getter
@Accessors(chain = true)
@AllArgsConstructor
public class StatusCode {
    private final Integer code;
    private final String msgKey;

    public String getTranslatedMsg(Object... args) {
        return I18nUtils.translateMsg(msgKey, args);
    }

    public static StatusCode $(Integer code, String msgKey) {
        return new StatusCode(code, msgKey);
    }

    /**
     * 动态传入code值，复用另一个StatusCode的msgKey(多语言的key)
     */
    public static StatusCode $(Integer code, StatusCode statusCode) {
        return new StatusCode(code, statusCode.getMsgKey());
    }

    public static boolean contains(Class<? extends BaseStatusCode> clazz, String codeName) {
        return Objects.nonNull(valueOf(clazz, codeName));
    }

    public static StatusCode valueOf(Class<? extends BaseStatusCode> clazz, String codeName) {
        Object code = ReflectUtil.getFieldValue(clazz, codeName);
        if (code instanceof StatusCode) {
            return (StatusCode) code;
        }
        return null;
    }
}

