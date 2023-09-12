package top.ysqorz.i18n.resolver.adapter;

import tech.sucore.config.CusConfigInfo;
import tech.sucore.config.EnvironmentInfo;
import top.ysqorz.i18n.common.CommonUtils;
import top.ysqorz.i18n.resolver.AbstractLocaleContextResolver;
import top.ysqorz.i18n.resolver.PropsLocaleContextResolver;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public class OREConfigLocaleContextResolver extends AbstractLocaleContextResolver {
    private final CusConfigInfo cusConfig;
    private final PropsLocaleContextResolver localeContextResolver;

    public OREConfigLocaleContextResolver(String installation) throws IOException {
        this.cusConfig = new CusConfigInfo(installation);
        File configFile = new File(EnvironmentInfo.getSystemConfigPath(), "i18n.properties");
        this.localeContextResolver = new PropsLocaleContextResolver(configFile);
    }

    @Override
    public Locale getLocaleContext() {
        String localeContextStr = cusConfig.getUserParam("i18n.locale.context");
        Locale localeContext = null;
        if (!CommonUtils.isEmpty(localeContextStr)) {
            localeContext = Locale.forLanguageTag(localeContextStr);
        }
        if (CommonUtils.isRootLocale(localeContext)) {
            localeContext = localeContextResolver.getLocaleContext();
        }
        return localeContext;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        String supportedLocalesStr = cusConfig.getUserParam("i18n.locale.supported");
        Set<Locale> supportedLocales = null;
        if (!CommonUtils.isEmpty(supportedLocalesStr)) {
            supportedLocales = parseSupportedLocales(supportedLocalesStr);
        }
        if (Objects.isNull(supportedLocales)) {
            supportedLocales = localeContextResolver.getSupportedLocales();
        }
        return supportedLocales;
    }

    public Set<Locale> parseSupportedLocales(String str) {
        return CommonUtils.splitStr(str, ",")
                .stream().map(Locale::forLanguageTag)
                .collect(Collectors.toSet());
    }
}
