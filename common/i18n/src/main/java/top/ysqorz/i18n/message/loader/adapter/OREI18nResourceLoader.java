package top.ysqorz.i18n.message.loader.adapter;

import tech.sucore.config.EnvironmentInfo;
import top.ysqorz.i18n.message.loader.I18nResourceLoader;

import java.io.File;

/**
 * 标准模块多语言文件：
 * ORE_ROOT/config/i18n/标准模块名_messages_zh_CN.properties
 * 客户化模块多语言文件：
 * ORE_ROOT/installations/实例名/config/i18n/客户化模块名__messages_zh_CN.properties
 *
 * @author yaoshiquan
 * @date 2023/9/1
 */
public class OREI18nResourceLoader implements I18nResourceLoader {
    private final String installation;

    public OREI18nResourceLoader(String installation) {
        this.installation = installation;
    }

    @Override
    public File getBundleFile(String resourceName, String format, ClassLoader loader) {
        File bundleFile = new File(getCustomI18nDir(), resourceName);
        if (!bundleFile.exists()) {
            bundleFile = new File(getStandardI18nDir(), resourceName);
        }
        return bundleFile;
    }

    public File getStandardI18nDir() {
        return new File(EnvironmentInfo.getSystemConfigPath(), "i18n");
    }

    public File getCustomI18nDir() {
        return new File(EnvironmentInfo.getInstallationConfigPath(installation), "i18n");
    }

    public String getInstallation() {
        return installation;
    }
}
