package top.ysqorz.i18n.message;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/24
 */
public interface ConstInterfaceGenerator extends AutoCloseable {
    /**
     * 用于在外部位置生成常量接口，可供没有源码的定制开发人员使用
     *
     * @param packagePath      常量接口的包路径，形如：tech.sucore.common.constant
     * @param destDir          常量接口的Java文件生成的目录
     * @param supportedLocales 参与生成常量接口的多语言
     */
    void generateConstInterfaces(String packagePath, File destDir, Locale... supportedLocales) throws IOException;
}
