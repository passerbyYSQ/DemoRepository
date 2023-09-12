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
public class PropsLocaleContextResolver extends AbstractLocaleContextResolver {
    private final File configFile;
    private Properties props;

    public PropsLocaleContextResolver() throws IOException {
        this("config/i18n.properties");
    }

    public PropsLocaleContextResolver(String configPath) throws IOException {
        this(CommonUtils.getClassPathResource(PropsLocaleContextResolver.class, configPath));
    }

    public PropsLocaleContextResolver(File configFile) throws IOException {
        this.configFile = configFile;
        loadProps();
    }

    public void loadProps() throws IOException {
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
