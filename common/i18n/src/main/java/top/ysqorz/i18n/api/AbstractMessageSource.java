package top.ysqorz.i18n.api;

import java.text.MessageFormat;
import java.util.*;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/18
 */
public abstract class AbstractMessageSource implements MessageSource {
    private MessageSource parentMessageSource;

    @Override
    public String getMessage(String code, Object[] args, Locale locale) {
        final MessageFormat messageFormat = getMessageFormat(code, locale);
        if (Objects.nonNull(messageFormat)) {
            // 有缓存。同MessageFormat不允许并发format，但是不同线程竞争同一个MessageFormat的几率非常小
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (messageFormat) {
                return messageFormat.format(resolveArgs(args));
            }
        }
        return getMessageFromParent(code, args, locale);
    }

    private String getMessageFromParent(String code, Object[] args, Locale locale) {
        return null;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String message = getMessage(code, args, locale);
        return Objects.isNull(message) ? defaultMessage : message;
    }

    protected Object[] resolveArgs(Object[] args) {
        return Objects.isNull(args) ? new Object[0] : args;
    }

    /**
     * 之所以不定义在AbstractResourceBundleMessageSource中，是为了即使使用DB方式实现MessageSource，也要强制实现类支持MessageFormat
     */
    protected abstract MessageFormat getMessageFormat(String code, Locale locale);

    @Override
    public void setParentMessageSource(MessageSource messageSource) {
        this.parentMessageSource = messageSource;
    }

    @Override
    public MessageSource getParentMessageSource() {
        return parentMessageSource;
    }

    @Override
    public MessageSource getRootMessageSource() {
        return null;
    }
}
