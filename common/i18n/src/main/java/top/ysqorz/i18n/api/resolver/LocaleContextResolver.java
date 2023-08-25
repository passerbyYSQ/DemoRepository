package top.ysqorz.i18n.api.resolver;

import java.util.List;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public interface LocaleContextResolver<T> {
    Locale resolveLocaleContext(T args);

    List<Locale> getSupportedLocales();
}
