package top.ysqorz.i18n.api;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

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
