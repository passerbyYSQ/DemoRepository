package top.ysqorz.i18n.message.loader;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/19
 */
public class ClassPathResourceLoader implements ResourceLoader {
    @Override
    public File getBundleFile(String resourceName, String format, ClassLoader loader) {
        File file = null;
        //判断resourceName是否包含.
        if (!resourceName.contains(".")) {
            resourceName = resourceName + "." + format;
        }
        // 判断传入loader是否为空
        if (loader == null) {
            loader = getClass().getClassLoader();
        }
        // 获取当前类的ClassLoader
        // 使用ClassLoader获取文件的URL
        URL url = loader.getResource(resourceName);
        if (url != null) {
            try {
                // 将URL转换为URI
                URI uri = url.toURI();
                // 创建File对象
                file = new File(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
