package top.ysqorz.i18n.message.loader;

import java.io.File;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public interface ResourceLoader {
    File getBundleFile(String resourceName, String format, ClassLoader loader);
}
