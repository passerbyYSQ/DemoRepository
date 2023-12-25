package top.ysqorz.i18n.message.loader;

import java.io.File;
import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public interface I18nResourceLoader {
    File getBundleFile(String resourceName, String format, ClassLoader loader) throws IOException;
}
