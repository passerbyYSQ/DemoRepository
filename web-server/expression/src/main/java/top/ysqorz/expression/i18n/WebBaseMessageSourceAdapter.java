package top.ysqorz.expression.i18n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.ysqorz.i18n.common.I18nUtils;
import top.ysqorz.i18n.message.loader.I18nResourceLoader;
import top.ysqorz.i18n.message.properties.ReloadableResourceBundleMessageSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/12/25
 */
@Component
public class WebBaseMessageSourceAdapter extends ReloadableResourceBundleMessageSource {
    @Value("${core.i18n.cache-millis:0}")
    private long cacheMillis;

    public static void main(String[] args) throws Exception {
        I18nUtils.generateConstInterfacesByCMD(args1 -> new WebBaseMessageSourceAdapter(), args);
    }

    /**
     * web服务的消息源不需要指明实例
     */
    public WebBaseMessageSourceAdapter() throws IOException {
        super(new WebBaseI18nResourceLoader(), StandardCharsets.UTF_8, Duration.ofMinutes(30).toMillis(), true);
        initCacheMillisFromConfig();
        initBasename();
    }

    public void initCacheMillisFromConfig() {
        // cacheMillis为0没有含义，忽略。如果想不缓存，请设置为 ResourceBundle.Control.TTL_DONT_CACHE，而非0
        if (cacheMillis != 0) {
            super.cacheMillis = cacheMillis;
        }
    }

    public void initBasename() {
        addBasename("WEB_messages");
    }

    public static class WebBaseI18nResourceLoader implements I18nResourceLoader {
        @Override
        public File getBundleFile(String resourceName, String format, ClassLoader loader) {
            return new File(I18nUtils.getStandardI18nDir(), resourceName);
        }
    }
}
