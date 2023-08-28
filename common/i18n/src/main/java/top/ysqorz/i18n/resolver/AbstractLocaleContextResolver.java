package top.ysqorz.i18n.resolver;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public abstract class AbstractLocaleContextResolver implements LocaleContextResolver {
    private Locale defaultLocale;
    private List<Locale> supportedLocales;

    public AbstractLocaleContextResolver() {
    }

    public AbstractLocaleContextResolver(Locale defaultLocale, List<Locale> supportedLocales) {
        this.defaultLocale = defaultLocale;
        this.supportedLocales = supportedLocales;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    @Override
    public Locale resolveLocaleContext() {
        Locale localeContext = getLocaleContext(); // 获取当前的语言环境
        if (Objects.isNull(localeContext)) {
            localeContext = defaultLocale;
        }
        List<Locale> supportedLocales = getSupportedLocales();
        if (Objects.isNull(supportedLocales) || supportedLocales.isEmpty() ||
                supportedLocales.contains(localeContext)) {
            return localeContext;
        }
        return null;
    }

    public abstract Locale getLocaleContext();

    @Override
    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }
}
