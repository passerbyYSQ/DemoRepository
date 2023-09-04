package top.ysqorz.i18n.resolver;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public abstract class AbstractLocaleContextResolver implements LocaleContextResolver {
    private Locale defaultLocale;
    private Locale localeContext;
    private Set<Locale> supportedLocales;

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    @Override
    public Locale resolveLocaleContext() {
        if (Objects.nonNull(localeContext)) {
            return localeContext; // 缓存
        }
        Locale localeContext = getLocaleContext();
        if (Objects.isNull(localeContext)) {
            if (Objects.isNull(defaultLocale)) {
                localeContext = Locale.getDefault();
            } else {
                localeContext = defaultLocale;
            }
        }
        Set<Locale> supportedLocales = resolveSupportedLocales();
        if (Objects.isNull(supportedLocales) || supportedLocales.isEmpty() ||
                supportedLocales.contains(localeContext)) {
            return this.localeContext = localeContext;
        }
        return null;
    }

    @Override
    public Set<Locale> resolveSupportedLocales() {
        if (Objects.nonNull(supportedLocales)) {
            return supportedLocales; // 缓存
        }
        return this.supportedLocales = getSupportedLocales();
    }

    public void clearCache() {
        localeContext = null;
        supportedLocales = null;
    }

    public abstract Locale getLocaleContext();

    public abstract Set<Locale> getSupportedLocales();
}
