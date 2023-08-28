package top.ysqorz.i18n.resolver;

import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public class RequestLocaleContextResolver extends AbstractLocaleContextResolver {
    @Override
    public Locale getLocaleContext() {
        // 请求头 -> 请求参数 -> 会话参数
        return null;
    }
}
