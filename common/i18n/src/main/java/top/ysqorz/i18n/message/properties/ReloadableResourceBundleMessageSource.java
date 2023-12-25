package top.ysqorz.i18n.message.properties;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import top.ysqorz.i18n.common.ConstInterfaceMeta;
import top.ysqorz.i18n.common.FileEventMonitor;
import top.ysqorz.i18n.message.contol.PropertyBundleControl;
import top.ysqorz.i18n.message.loader.I18nResourceLoader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 可重新载入的资源包消息源，支持缓存过期和及时更新
 *
 * @author yaoshiquan
 * @date 2023/8/18
 */
public class ReloadableResourceBundleMessageSource extends ResourceBundleMessageSource
        implements PropertyBundleControl.BundleControlCallback,
        FileEventMonitor.FileEventCallback<ReloadableResourceBundleMessageSource.WatchedFilePayload> {

    private static final Logger log = Logger.getLogger(ReloadableResourceBundleMessageSource.class.getSimpleName());

    /**
     * ResourceBundleHolder缓存，一级key为basename，二级key为locale
     */
    private final Map<String, Map<Locale, ResourceBundleHolder>> cachedResourceBundles = new ConcurrentHashMap<>();

    /**
     * 缓存的有效期
     */
    protected long cacheMillis;

    /**
     * 资源文件监听器，通过监听文件修改来及时更新缓存
     */
    private FileEventMonitor<WatchedFilePayload> fileEventMonitor;

    public ReloadableResourceBundleMessageSource(I18nResourceLoader resourceLoader) throws IOException {
        this(resourceLoader, StandardCharsets.UTF_8, PropertyBundleControl.TTL_NO_EXPIRATION_CONTROL, false);
    }

    /**
     * @param resourceLoader 资源加载器，用于自定义查找资源文件
     * @param encoding       资源文件的编码方式
     * @param cacheMillis    缓存的有效期
     * @param enableMonitor  是否开启资源文件监听器
     */
    public ReloadableResourceBundleMessageSource(I18nResourceLoader resourceLoader, Charset encoding, long cacheMillis,
                                                 boolean enableMonitor) throws IOException {
        // 由于ResourceBundle没有提供API清除单个ResourceBundle的缓存，因此禁用ResourceBundle内部缓存，由当前类来统一管理缓存
        super(new PropertyBundleControl(resourceLoader, encoding, PropertyBundleControl.TTL_DONT_CACHE));
        this.cacheMillis = cacheMillis;
        if (enableMonitor) {
            this.fileEventMonitor = new FileEventMonitor<>(200L);
            ((PropertyBundleControl) this.control).setControlCallback(this);
            this.fileEventMonitor.startWatch(this);
        }
    }

    @Override
    protected MessageFormat getMessageFormat(String code, Locale locale) {
        for (String basename : basenameSet) {
            ResourceBundleHolder bundleHolder = getResourceBundleHolder(basename, locale);
            if (Objects.isNull(bundleHolder)) {
                continue;
            }
            MessageFormat messageFormat = bundleHolder.getMessageFormat(code);
            if (Objects.nonNull(messageFormat)) {
                return messageFormat;
            }
        }
        return null;
    }

    public ResourceBundleHolder getResourceBundleHolder(String basename, Locale locale) {
        Map<Locale, ResourceBundleHolder> bundleMap = cachedResourceBundles.computeIfAbsent(basename, s -> new ConcurrentHashMap<>());
        ResourceBundleHolder bundleHolder = bundleMap.get(locale);
        if (Objects.nonNull(bundleHolder)) {
            if (bundleHolder.isExpired()) { // 缓存已过期，则刷新
                bundleHolder.refresh(true);
            }
            log.log(Level.WARNING, String.format("Get resource bundle from cache, basename: %s, locale: %s", basename, locale));
        } else {
            if (Objects.isNull(bundleHolder = bundleMap.get(locale))) { // 双重检测锁
                synchronized (cachedResourceBundles) {
                    if (Objects.isNull(bundleHolder = bundleMap.get(locale))) {
                        ResourceBundle bundle = getResourceBundle(basename, locale);// 已禁用缓存，载入ResourceBundle
                        if (Objects.isNull(bundle)) {
                            return null;
                        }
                        log.log(Level.WARNING, String.format("Put resource bundle into cache, basename: %s, locale: %s", basename, locale));
                        bundleHolder = new ResourceBundleHolder(bundle);
                        bundleMap.put(locale, bundleHolder);
                    }
                }
            }
        }
        return bundleHolder;
    }

    @Override
    public void addMessage(String code, String value, Locale locale) {

    }

    @Override
    public void setMessage(String code, String value, Locale local) {

    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(fileEventMonitor)) {
            fileEventMonitor.close();
        }
        cachedResourceBundles.values().forEach(Map::clear);
        cachedResourceBundles.clear();
    }

    public static class WatchedFilePayload {
        private final String basename;
        private final Locale locale;

        public WatchedFilePayload(String basename, Locale locale) {
            this.basename = basename;
            this.locale = locale;
        }
    }

    /**
     * 加载资源文件并建ResourceBundle对象时会回调到该方法
     */
    @Override
    public void onResourceBundleCreated(File bundleFile, ResourceBundle bundle, String basename, Locale locale) { // 此时的ResourceBundle还是半成品
        try {
            // 第一次载入和后续的重新载入都会回调到这里，watch内部会防止文件重复监听
            fileEventMonitor.watch(bundleFile, new WatchedFilePayload(basename, locale), StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEventOccurred(File bundleFile, WatchedFilePayload extra, WatchEvent.Kind<?> eventKind) {
        if (!StandardWatchEventKinds.ENTRY_MODIFY.equals(eventKind)) {
            return;
        }
        Map<Locale, ResourceBundleHolder> holderMap = cachedResourceBundles.get(extra.basename);
        if (Objects.isNull(holderMap)) {
            return;
        }
        ResourceBundleHolder bundleHolder = holderMap.get(extra.locale);
        if (Objects.isNull(bundleHolder)) {
            return;
        }
        log.info(String.format("File modified: %s", bundleFile.getAbsolutePath()));
        bundleHolder.refresh(false);
    }

    public List<ConstInterfaceMeta> loadAllCodes(String packagePath, Locale... supportedLocales) {
        try {
            PropertyBundleControl control = (PropertyBundleControl) this.control;
            List<ConstInterfaceMeta> constInterfaceMetas = new ArrayList<>();
            for (String basename : basenameSet) {
                ConstInterfaceMeta constInterfaceMeta = new ConstInterfaceMeta(packagePath, basename);
                for (Locale locale : supportedLocales) {
                    ResourceBundle bundle = control.newBundle(basename, locale, null, classLoader, true);
                    if (Objects.isNull(bundle)) {
                        continue;
                    }
                    Enumeration<String> codes = bundle.getKeys();
                    while (codes.hasMoreElements()) {
                        String code = codes.nextElement();
                        constInterfaceMeta.addProps(code, code);
                    }
                }
                constInterfaceMetas.add(constInterfaceMeta);
            }
            return constInterfaceMetas;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void generateConstInterfaces(String packagePath, File destDir, Locale... supportedLocales) throws IOException {
        Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        config.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "template"));
        Template template = config.getTemplate("const_interface.ftl");
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IOException("Create dirs failed: " + destDir.getAbsolutePath());
            }
        }
        List<ConstInterfaceMeta> constInterfaceMetas = loadAllCodes(packagePath, supportedLocales);
        if (Objects.isNull(constInterfaceMetas)) {
            return;
        }
        for (ConstInterfaceMeta constInterfaceMeta : constInterfaceMetas) {
            File javaFile = new File(destDir, constInterfaceMeta.getClassName() + ".java");
            try (OutputStream outputStream = Files.newOutputStream(javaFile.toPath());
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
                template.process(constInterfaceMeta, writer);
                log.info("Constant interface generated in " + javaFile.getAbsolutePath());
            } catch (TemplateException e) {
                throw new IOException(e);
            }
        }
    }

    public class ResourceBundleHolder {
        private ResourceBundle resourceBundle;

        /**
         * 当前ResourceBundle下一次过期的时间戳
         */
        private long expiredTimeStamp;

        private final ReentrantLock refreshLock = new ReentrantLock();

        /**
         * 当前ResourceBundle的MessageFormat缓存，key为code(键值对的key)
         */
        private final Map<String, MessageFormat> cachedMessageFormats = new HashMap<>();

        public ResourceBundleHolder(ResourceBundle bundle) {
            this.resourceBundle = bundle;
            updateNextExpiredTimestamp();
        }

        public MessageFormat getMessageFormat(String code) {
            MessageFormat messageFormat = cachedMessageFormats.get(code);
            if (Objects.nonNull(messageFormat)) {
                return messageFormat;
            }
            refreshLock.lock(); // 存入MessageFormat，和刷新时使用同一把锁
            try {
                if (Objects.isNull(messageFormat = cachedMessageFormats.get(code))) {
                    if (!resourceBundle.containsKey(code)) {
                        return null;
                    }
                    String pattern = resourceBundle.getString(code);
                    messageFormat = new MessageFormat(pattern, resourceBundle.getLocale());
                    cachedMessageFormats.put(code, messageFormat);
                }
                return messageFormat;
            } finally {
                refreshLock.unlock();
            }
        }

        public boolean isExpired() {
            return expiredTimeStamp != PropertyBundleControl.TTL_NO_EXPIRATION_CONTROL && System.currentTimeMillis() > expiredTimeStamp;
        }

        public void refresh(boolean checkExpired) {
            refreshLock.lock();
            try {
                if (!checkExpired || isExpired()) {
                    doRefresh();
                }
            } finally {
                refreshLock.unlock();
            }
        }

        private void updateNextExpiredTimestamp() {
            if (cacheMillis >= 0) {
                expiredTimeStamp = System.currentTimeMillis() + cacheMillis;
            } else {
                expiredTimeStamp = cacheMillis; // 永不过期 or 不缓存
            }
        }

        private void doRefresh() {
            String basename = resourceBundle.getBaseBundleName();
            Locale locale = resourceBundle.getLocale();
            resourceBundle = getResourceBundle(basename, locale); // 已禁用缓存，载入ResourceBundle
            if (Objects.isNull(resourceBundle)) {
                log.log(Level.WARNING, String.format("Refresh failed, resource file missing, basename: %s, locale: %s", basename, locale));
                return;
            }
            cachedMessageFormats.clear();
            Enumeration<String> keys = resourceBundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String pattern = resourceBundle.getString(key);
                cachedMessageFormats.put(key, new MessageFormat(pattern, resourceBundle.getLocale()));
            }
            updateNextExpiredTimestamp();
            log.info(String.format("Resource bundle refreshed, basename: %s, locale: %s, expiredTimeStamp: %s",
                    basename, locale, new Date(expiredTimeStamp)));
        }
    }
}
