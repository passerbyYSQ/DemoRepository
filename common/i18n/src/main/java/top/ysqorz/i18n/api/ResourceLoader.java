package top.ysqorz.i18n.api;

import java.io.File;
import java.io.InputStream;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public interface ResourceLoader {
    File getBundleFile(String resourceName, String format, ClassLoader loader);
}
