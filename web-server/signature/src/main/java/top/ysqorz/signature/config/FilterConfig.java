package top.ysqorz.signature.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ysqorz.signature.model.SignatureProps;

@Configuration
public class FilterConfig {
    @ConditionalOnBean(SignatureProps.class)
    @Bean
    public FilterRegistrationBean<RequestCachingFilter> requestCachingFilterRegistration(
            RequestCachingFilter requestCachingFilter) {
        FilterRegistrationBean<RequestCachingFilter> bean = new FilterRegistrationBean<>(requestCachingFilter);
        bean.setOrder(1);
        return bean;
    }
}
