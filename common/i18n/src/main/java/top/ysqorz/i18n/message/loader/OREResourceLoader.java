package top.ysqorz.i18n.message.loader;

import java.io.File;
import java.util.Objects;

/**
 * 标准模块多语言文件：
 * ORE_ROOT/config/i18n/标准模块名/
 * 客户化模块多语言文件：
 * ORE_ROOT/installations/实例名/config/i18n/客户化模块名/
 *
 * @author yaoshiquan
 * @date 2023/9/1
 */
public class OREResourceLoader implements ResourceLoader {
    private final String module;
    private final String installation;

    public OREResourceLoader(String module) {
        this(null, module);
    }

    public OREResourceLoader(String installation, String module) {
        this.installation = installation;
        this.module = module;
    }

    public boolean isStandardModule() {
        return Objects.isNull(installation);
    }

    @Override
    public File getBundleFile(String resourceName, String format, ClassLoader loader) {
        return null;
    }
}
