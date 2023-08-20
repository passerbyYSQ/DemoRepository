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
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/18
 */
public class ReloadableResourceBundleMessageSource extends ResourceBundleMessageSource
        implements PropertyBundleControl.BundleControlCallback,
        FileEventMonitor.FileEventCallback<ReloadableResourceBundleMessageSource.ResourceBundleHolder> {
    // basename locale
    private final Map<String, Map<Locale, ResourceBundleHolder>> cachedResourceBundles = new ConcurrentHashMap<>();
    private final long cacheMillis;
    private FileEventMonitor<ResourceBundleHolder> fileEventMonitor;

    public ReloadableResourceBundleMessageSource(ResourceLoader resourceLoader) throws IOException {
        this(resourceLoader, StandardCharsets.UTF_8, PropertyBundleControl.TTL_NO_EXPIRATION_CONTROL, false);
    }

    public ReloadableResourceBundleMessageSource(ResourceLoader resourceLoader, Charset encoding, long cacheMillis,
                                                 boolean enableMonitor) throws IOException {
        // 由于ResourceBundle没有提供API清除单个Bundle的缓存，因此禁用ResourceBundle内部缓存，由当前类来管理缓存
        super(new PropertyBundleControl(resourceLoader, encoding, PropertyBundleControl.TTL_DONT_CACHE));
        this.cacheMillis = cacheMillis;
        if (enableMonitor) {
            this.fileEventMonitor = new FileEventMonitor<>(500L); // 500ms 扫描监听的文件是否更新，如果发现更新则处理
            this.control.setControlCallback(this);
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

    @Override
    public void onResourceBundleCreated(File bundleFile, ResourceBundle bundle) {
        if (Objects.isNull(fileEventMonitor)) {
            return; // 未开启该功能
        }
        try {
            // 1. 第一次载入 2. reload
            fileEventMonitor.unWatch(bundleFile); // 如果是刷新，那么之前应该已经添加过监听，将监听先移除掉
            ResourceBundleHolder bundleHolder = getResourceBundleHolder(bundle);
            fileEventMonitor.watch(bundleFile, bundleHolder, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
            // 监听失败
        }
    }

    @Override
    public void onEventOccurred(File file, ResourceBundleHolder bundleHolder, List<WatchEvent.Kind<?>> eventKinds) {
        if (eventKinds.stream().anyMatch(StandardWatchEventKinds.ENTRY_MODIFY::equals)) {
            bundleHolder.refresh();
        }
    }

    public class ResourceBundleHolder {
        // basename-locale 确认一个ResourceBundle
        private ResourceBundle resourceBundle;
        private long expiredTimeStamp;
        private final ReentrantLock refreshLock = new ReentrantLock();
        // locale确认后，pattern唯一；由于加锁成功后才能对cachedMessageFormats进行更新，所以不需要使用ConcurrentHashMap
        private final Map<String, MessageFormat> cachedMessageFormats = new HashMap<>();

        public ResourceBundleHolder(String basename, Locale locale) {
            this.resourceBundle = getResourceBundle(basename, locale); // 禁用缓存，载入Bundle
            updateLastRefreshTimestamp();
        }

        public MessageFormat getMessageFormat(String code) {
            MessageFormat messageFormat = cachedMessageFormats.get(code);
            if (Objects.nonNull(messageFormat)) {
                return messageFormat;
            }
            refreshLock.lock(); // 存入MessageFormat，和刷新时使用同一把锁
            try {
                if (Objects.isNull(messageFormat = cachedMessageFormats.get(code))) {
                    String pattern = resourceBundle.getString(code);
                    if (!resourceBundle.containsKey(code)) {
                        return null;
                    }
                    messageFormat = new MessageFormat(pattern, resourceBundle.getLocale());
                    cachedMessageFormats.put(pattern, messageFormat);
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

        private void updateLastRefreshTimestamp() {
            if (cacheMillis >= 0) {
                // 更新最后一次刷新的时间
                expiredTimeStamp = System.currentTimeMillis() + cacheMillis;
            } else {
                // 永不过期 or 不缓存
                expiredTimeStamp = cacheMillis;
            }
        }

        private void doRefresh() {
            resourceBundle = getResourceBundle(resourceBundle.getBaseBundleName(), resourceBundle.getLocale()); // 禁用缓存，重新载入
            cachedMessageFormats.clear();
            Enumeration<String> keys = resourceBundle.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                String pattern = resourceBundle.getString(key);
                cachedMessageFormats.put(pattern, new MessageFormat(pattern, resourceBundle.getLocale()));
            }
            updateLastRefreshTimestamp();
        }
    }
}
