package top.ysqorz.i18n.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可重新载入的资源包消息源，支持缓存过期和及时更新
 *
 * @author yaoshiquan
 * @date 2023/8/18
 */
public class ReloadableResourceBundleMessageSource extends ResourceBundleMessageSource
        implements PropertyBundleControl.BundleControlCallback,
        FileEventMonitor.FileEventCallback<ReloadableResourceBundleMessageSource.ResourceBundleHolder> {
    /**
     * ResourceBundleHolder缓存，一级key为basename，二级key为locale
     */
    private final Map<String, Map<Locale, ResourceBundleHolder>> cachedResourceBundles = new ConcurrentHashMap<>();

    /**
     * 缓存的有效期
     */
    private final long cacheMillis;

    /**
     * 资源文件监听器，通过监听文件修改来及时更新缓存
     */
    private FileEventMonitor<ResourceBundleHolder> fileEventMonitor;

    public ReloadableResourceBundleMessageSource(ResourceLoader resourceLoader) throws IOException {
        this(resourceLoader, StandardCharsets.UTF_8, PropertyBundleControl.TTL_NO_EXPIRATION_CONTROL, false);
    }

    /**
     * @param resourceLoader 资源加载器，用于自定义查找资源文件
     * @param encoding       资源文件的编码方式
     * @param cacheMillis    缓存的有效期
     * @param enableMonitor  是否开启资源文件监听器
     */
    public ReloadableResourceBundleMessageSource(ResourceLoader resourceLoader, Charset encoding, long cacheMillis,
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
                bundleHolder.refresh();
            }
        } else {
            if (Objects.isNull(bundleHolder = bundleMap.get(locale))) { // 双重检测锁
                synchronized (cachedResourceBundles) {
                    if (Objects.isNull(bundleHolder = bundleMap.get(locale))) {
                        bundleHolder = new ResourceBundleHolder(basename, locale);
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

    private ResourceBundleHolder getResourceBundleHolder(ResourceBundle bundle) {
        Map<Locale, ResourceBundleHolder> bundleMap = cachedResourceBundles.get(bundle.getBaseBundleName());
        if (Objects.isNull(bundleMap)) {
            return null;
        }
        return bundleMap.get(bundle.getLocale());
    }

    /**
     * 加载资源文件并建ResourceBundle对象时会回调到该方法
     */
    @Override
    public void onResourceBundleCreated(File bundleFile, ResourceBundle bundle) {
        if (Objects.isNull(fileEventMonitor)) {
            return;
        }
        try {
            // 第一次载入和后续的重新载入都会回调到这里，watch内部会防止文件重复监听
            ResourceBundleHolder bundleHolder = getResourceBundleHolder(bundle);
            fileEventMonitor.watch(bundleFile, bundleHolder, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEventOccurred(File bundleFile, ResourceBundleHolder bundleHolder, WatchEvent.Kind<?> eventKind) {
        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(eventKind)) {
            bundleHolder.refresh();
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

        public ResourceBundleHolder(String basename, Locale locale) {
            this.resourceBundle = getResourceBundle(basename, locale); // 已禁用缓存，载入ResourceBundle
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

        public void refresh() {
            // 尝试加锁失败，可能其他线程已经持锁在刷新，所以当前线程不需要等待去刷新，直接返回
            // 独占加锁之后必须再判断是否过期
            if (!refreshLock.tryLock() || !isExpired()) {
                return;
            }
            try {
                doRefresh();
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
            resourceBundle = getResourceBundle(resourceBundle.getBaseBundleName(), resourceBundle.getLocale()); // 已禁用缓存，载入ResourceBundle
            cachedMessageFormats.clear();
            Enumeration<String> keys = resourceBundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String pattern = resourceBundle.getString(key);
                cachedMessageFormats.put(key, new MessageFormat(pattern, resourceBundle.getLocale()));
            }
            updateNextExpiredTimestamp();
        }
    }
}