package top.ysqorz.i18n.message;

import java.util.Locale;

/**
 * @author yaoshiquan
 * @date 2023/8/17
 */
public interface MessageSource {

    String getMessage(String code, String defaultMessage, Locale locale, Object... args);

    String getMessage(String code, Locale locale, Object... args);

    void addMessage(String code, String value, Locale locale);

    void setMessage(String code, String value, Locale local);

    void setParentMessageSource(MessageSource messageSource);

    MessageSource getParentMessageSource();

    MessageSource getRootMessageSource();
}
