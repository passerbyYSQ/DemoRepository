package top.ysqorz.i18n.api.resolver;

import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public class OREConfigLocaleContextResolver extends AbstractLocaleContextResolver<Void> {
    @Override
    public Locale getLocaleContext(Void args) {
        // 从ZWTeamworks的安装目录下读取多语言配置
        return null;
    }
}
