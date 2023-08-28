package top.ysqorz.i18n.resolver;

import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public class OREConfigLocaleContextResolver extends AbstractLocaleContextResolver {
    @Override
    public Locale getLocaleContext() {
        // 从ZWTeamworks的安装目录下读取多语言配置
        return null;
    }
}
