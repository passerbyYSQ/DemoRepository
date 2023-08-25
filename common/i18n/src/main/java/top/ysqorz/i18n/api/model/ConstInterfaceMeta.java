package top.ysqorz.i18n.api.model;

import top.ysqorz.i18n.api.common.CommonUtils;

import java.util.HashMap;

/**
 * key为变量名，value为多语言的code
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public class ConstInterfaceMeta extends HashMap<String, String> {
    private final String packagePath;
    private final String className;

    public ConstInterfaceMeta(String packagePath, String className) {
        this.packagePath = packagePath;
        this.className = CommonUtils.camelToScreamingSnake(className);
    }

    @Override
    public String put(String key, String value) {
        return super.put(CommonUtils.camelToScreamingSnake(key), value);
    }
}
