package top.ysqorz.i18n.message.contol;

import top.ysqorz.i18n.message.loader.I18nResourceLoader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * 单例即可
 *
 * @author yaoshiquan
 * @date 2023/8/18
 */
public class PropertyBundleControl extends ResourceBundle.Control {
    /**
     * 资源文件的编码，默认utf-8，防止中文乱码
     */
    private Charset encoding = StandardCharsets.UTF_8;
    /**
     * ResourceBundle的缓存时间，默认永不过期
     */
    private long cacheMillis = TTL_NO_EXPIRATION_CONTROL;
    private final I18nResourceLoader resourceLoader;
    private BundleControlCallback callback;

    public PropertyBundleControl(I18nResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public PropertyBundleControl(I18nResourceLoader resourceLoader, Charset encoding, long cacheMillis) {
        this.resourceLoader = resourceLoader;
        this.encoding = encoding;
        this.cacheMillis = cacheMillis;
    }

    public void setControlCallback(BundleControlCallback callback) {
        this.callback = callback;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    // 不提供setter，后期更改cacheMillis，ResourceBundle中的缓存是不会受到影响的
    public long getCacheMillis() {
        return cacheMillis;
    }

    // 由于构造方法必须传入ResourceLoader，因此无需提供setter
    public I18nResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public ResourceBundle newBundle(String basename, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
        String bundleName = toBundleName(basename, locale);
        String resourceName = toResourceName(bundleName, "properties");
        File bundleFile = resourceLoader.getBundleFile(resourceName, format, loader);
        if (Objects.isNull(bundleFile) || !bundleFile.exists()) {
            return null;
        }
        InputStream inputStream = Files.newInputStream(bundleFile.toPath());
        PropertyResourceBundle bundle = new PropertyResourceBundle(new InputStreamReader(inputStream, encoding)); // 注意是半成品
        if (Objects.nonNull(callback)) {
            callback.onResourceBundleCreated(bundleFile, bundle, basename, locale);
        }
        return bundle;
    }

    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
        return null;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return Collections.singletonList("java.properties");
    }

    @Override
    public long getTimeToLive(String basename, Locale locale) {
        return cacheMillis;
    }

    public interface BundleControlCallback {
        void onResourceBundleCreated(File bundleFile, ResourceBundle bundle, String basename, Locale locale);
    }
}
