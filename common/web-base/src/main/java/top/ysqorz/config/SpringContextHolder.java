package top.ysqorz.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Component
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static final PropertyNamingStrategy.PropertyNamingStrategyBase
            snakeCaseStrategy = new PropertyNamingStrategy.SnakeCaseStrategy();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextHolder.applicationContext == null) {
            SpringContextHolder.applicationContext = applicationContext;
            log.info("Cache IOC container successfully."); // 缺少@Component注解，不会被调用
        }
    }

    // 获取IOC容器
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // 通过name获取Bean
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    // 通过class获取Bean
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> Map<String, T> getBeanMap(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    public static ObjectMapper getObjectMapper() {
        return getBean(ObjectMapper.class);
    }

    // 通过name以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    // 将变量名转成下划线命名方式
    public static String snakeCaseFormat(String variableName) {
        return snakeCaseStrategy.translate(variableName);
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static void resetRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public static <T> T getRequestAttribute(String key, Class<T> clazz) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return clazz.cast(requestAttributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST));
    }

    public static String getRequestHeader(String name) {
        return getRequest().getHeader(name);
    }
}
