package top.ysqorz.i18n;

import org.junit.Before;
import org.junit.Test;
import top.ysqorz.i18n.message.loader.ClassPathResourceLoader;
import top.ysqorz.i18n.message.properties.ReloadableResourceBundleMessageSource;
import top.ysqorz.i18n.message.properties.ResourceBundleMessageSource;
import top.ysqorz.i18n.message.properties.adapter.OREMessageSourceAdapter;
import top.ysqorz.i18n.resolver.PropsLocaleContextResolver;
import top.ysqorz.i18n.resolver.LocaleContextResolver;

import java.io.IOException;
import java.util.Locale;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/4
 */
public class MessageSourceTest {
    private ClassPathResourceLoader resourceLoader;
    private LocaleContextResolver localeContextResolver;

    @Before
    public void init() throws IOException {
        resourceLoader = new ClassPathResourceLoader();
        localeContextResolver = new PropsLocaleContextResolver();
    }

    @Test
    public void testLocale() {
        System.out.println(Locale.SIMPLIFIED_CHINESE);
        System.out.println(Locale.forLanguageTag("zh-CN"));
    }

    /**
     * 测试生成常量接口
     */
    @Test
    public void testGenerateConstInterfaces() throws IOException {
        ResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource(resourceLoader);
        messageSource.addBasename("i18n/messages");
        Locale[] supportedLocales = localeContextResolver.resolveSupportedLocales().toArray(new Locale[0]);
        messageSource.generateConstInterfaces("omf", supportedLocales);
    }

    @Test
    public void testOREMessageSource() throws IOException {
        OREMessageSourceAdapter.main(null);
    }
}
