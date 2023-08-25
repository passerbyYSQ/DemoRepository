package top.ysqorz.i18n.api;

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
        this(control, null);
    }

    public ResourceBundleMessageSource(ResourceBundle.Control control, ClassLoader classLoader) {
        this.control = control;
        this.classLoader = Objects.isNull(classLoader) ? this.getClass().getClassLoader() : classLoader;
    }

    protected ResourceBundle getResourceBundle(String basename, Locale locale) {
        return ResourceBundle.getBundle(basename, locale, classLoader, control); // ResourceBundle内部有缓存
    }

    public void addBasename(String... basename) {
    }

    public void setBasenameSet(LinkedHashSet<String> basenameSet) {
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
