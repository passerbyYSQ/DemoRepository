package top.ysqorz.i18n.resolver;

import java.util.List;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public interface LocaleContextResolver {
    Locale resolveLocaleContext();

    List<Locale> getSupportedLocales();
}
