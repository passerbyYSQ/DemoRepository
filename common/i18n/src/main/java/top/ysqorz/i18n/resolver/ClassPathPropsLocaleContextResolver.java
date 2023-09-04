package top.ysqorz.i18n.resolver;

import top.ysqorz.i18n.common.CommonUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/28
 */
public class ClassPathPropsLocaleContextResolver extends AbstractLocaleContextResolver {
    private final String configPath;
    private Properties props;

    public ClassPathPropsLocaleContextResolver() throws IOException {
        this("config/locale.properties");
    }

    public ClassPathPropsLocaleContextResolver(String configPath) throws IOException {
        this.configPath = configPath;
        loadProps();
    }

    public void loadProps() throws IOException {
        File configFile = CommonUtils.getClassPathResource(getClass(), configPath);
        if (Objects.isNull(configFile) || !configFile.exists()) {
            throw new FileNotFoundException();
        }
        props = new Properties();
        try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
            props.load(bufReader);
        }
    }

    @Override
    public Locale getLocaleContext() {
        String localeContextStr = props.getProperty("locale.context");
        if (Objects.isNull(localeContextStr)) {
            return null;
        }
        return Locale.forLanguageTag(localeContextStr);
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        String supportedLocalesStr = props.getProperty("locale.supported");
        if (Objects.isNull(supportedLocalesStr)) {
            return null;
        }
        return CommonUtils.splitStr(supportedLocalesStr, ",")
                .stream().map(Locale::forLanguageTag)
                .collect(Collectors.toSet());
    }
}
