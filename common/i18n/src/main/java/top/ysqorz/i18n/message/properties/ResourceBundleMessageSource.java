package top.ysqorz.i18n.message.properties;

import top.ysqorz.i18n.message.AbstractMessageSource;

import java.util.*;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/18
 */
public abstract class ResourceBundleMessageSource extends AbstractMessageSource {
    /**
     * 多个资源包的前缀，确保顺序和唯一
     */
    protected Set<String> basenameSet = new LinkedHashSet<>();
    protected ResourceBundle.Control control;
    protected ClassLoader classLoader;

    public ResourceBundleMessageSource(ResourceBundle.Control control) {
        this.control = control;
    }

    protected ResourceBundle getResourceBundle(String basename, Locale locale) {
        if (Objects.isNull(classLoader)) {
            classLoader = getClass().getClassLoader();
        }
        try {
            return ResourceBundle.getBundle(basename, locale, classLoader, control); // ResourceBundle内部有缓存
        } catch (Exception e) {
            e.printStackTrace(); // 获取不到会抛出异常MissingResourceException
            return null;
        }
    }

    public void addBasename(String... basename) {
        basenameSet.addAll(Arrays.asList(basename));
    }

    public Set<String> getBasenameSet() {
        return basenameSet;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
