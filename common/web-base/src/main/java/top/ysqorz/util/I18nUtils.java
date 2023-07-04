package top.ysqorz.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.config.SpringContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class I18nUtils {
    private static MessageSource messageSource;
    private static boolean open;
    private static String language;

    public I18nUtils(Map<String, ResourceBundleMessageSource> messageSourceMap,
                     @Value("${core.i18n.open:true}") boolean open,
                     @Value("${core.i18n.default:zh-CN}") String language) {
        I18nUtils.messageSource = pickLeafMessageSource(messageSourceMap);
        I18nUtils.open = open;
        I18nUtils.language = language;
    }

    /**
     * 获取当前容器中叶子节点的MessageSource
     */
    private MessageSource pickLeafMessageSource(Map<String, ResourceBundleMessageSource> messageSourceMap) {
        Map<MessageSource, List<ResourceBundleMessageSource>> childrenMessageSource = new HashMap<>();
        for (ResourceBundleMessageSource source : messageSourceMap.values()) {
            // 如果是根节点，那么key为null
            childrenMessageSource.computeIfAbsent(source.getParentMessageSource(), messageSource -> new ArrayList<>()).add(source);
        }
        for (ResourceBundleMessageSource source : messageSourceMap.values()) {
            if (ObjectUtils.isEmpty(childrenMessageSource.get(source))) {
                return source;
            }
        }
        return null;
    }

    public static MessageSource getActualMessageSource() {
        return messageSource;
    }

    /**
     * 是否开启国际化
     */
    public static boolean isI18nOpen() {
        return open;
    }

    /**
     * 翻译提示信息，可以传入参数
     *
     * properties中文件定义：welcome.message = Welcome, {0}!
     * 获取对应的翻译：translateMsg("welcome.message", "zhangsan")
     */
    public static String translateMsg(String key, Object... args) {
        assert key != null;
        if (!open) {
            return key;
        }
        String translated = null;
        try {
            translated = messageSource.getMessage(key, args, key, getLocale());
        } catch (NoSuchMessageException ignored) {
        }
        if (ObjectUtils.isEmpty(translated)) {
            translated = key;
        }
        return translated;
    }

    /**
     * 翻译并拼接多个无参数的提示信息
     * StatusCode.UI_SAVE_FAIL, "1233", StatusCode.UI_BUILD_FAIL
     *
     * @param codeList 错误码或者字符串列表
     * @return 翻译之后并拼接起来的结果
     */
    public static String translateMsg(Object... codeList) {
        return Arrays.stream(codeList).map(code -> {
            if (code instanceof StatusCode) {
                return ((StatusCode) code).getMsgKey();
            } else if (code instanceof String) {
                return (String) code; // 动态的字符串
            } else {
                return code.toString();
            }
        }).map(I18nUtils::translateMsg).collect(Collectors.joining());
    }

    /**
     * 获取当前的语言环境
     */
    public static Locale getLocale() {
        Locale defaultLocale = Locale.forLanguageTag(language);
        String langHeader = SpringContextHolder.getRequest().getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (ObjectUtils.isEmpty(langHeader)) {
            return defaultLocale;
        }
        return LocaleContextHolder.getLocale();
    }
}
