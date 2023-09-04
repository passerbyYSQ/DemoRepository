package top.ysqorz.i18n.resolver;

import java.util.Locale;
import java.util.Set;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public interface LocaleContextResolver {
    Locale resolveLocaleContext();

    Set<Locale> resolveSupportedLocales();
}
