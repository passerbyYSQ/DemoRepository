package top.ysqorz.i18n.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * key为变量名，value为多语言的code
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public class ConstInterfaceMeta {
    private final String packagePath;
    private final String className;
    // key 常量名，value常量值
    private final Map<String, String> props = new LinkedHashMap<>();

    public ConstInterfaceMeta(String packagePath, String className) {
        this.packagePath = packagePath;
        this.className = I18nUtils.toUpperCamelCase(className) + "Code";
    }

    public void addProps(String key, String value) {
        props.put(I18nUtils.toScreamingSnake(key), value);
    }

    public Map<String, String> getProps() {
        return props;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public String getClassName() {
        return className;
    }
}
