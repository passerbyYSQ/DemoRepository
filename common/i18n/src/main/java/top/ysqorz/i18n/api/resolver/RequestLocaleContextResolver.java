package top.ysqorz.i18n.api.resolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/23
 */
public class RequestLocaleContextResolver implements LocaleContextResolver<HttpServletRequest> {
    @Override
    public Locale resolveLocaleContext(HttpServletRequest request) {
        // 请求头 -> 请求参数 -> 会话参数
        return null;
    }
}
