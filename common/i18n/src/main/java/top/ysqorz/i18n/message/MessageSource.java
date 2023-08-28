package top.ysqorz.i18n.message;

import java.util.Locale;

/**
 * @author yaoshiquan
 * @date 2023/8/17
 */
public interface MessageSource {
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    String getMessage(String code, Object[] args, Locale locale);

    void addMessage(String code, String value, Locale locale);

    void setMessage(String code, String value, Locale local);

    void setParentMessageSource(MessageSource messageSource);

    MessageSource getParentMessageSource();

    MessageSource getRootMessageSource();
}
