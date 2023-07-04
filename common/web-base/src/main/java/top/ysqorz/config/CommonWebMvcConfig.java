package top.ysqorz.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CommonWebMvcConfig implements WebMvcConfigurer {

    /**
     * 处理跨域问题。addInterceptors()的拦截器会导致此处的跨域配置失效
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // .allowedOrigins("*")  // 放行哪些原始域  using "allowedOriginPatterns" instead
                .allowedOrigins("*")
                .allowCredentials(true) // 是否发送cookie
                .allowedMethods("OPTIONS", "HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
                .exposedHeaders("*")
                .allowedHeaders("*") // allowedHeaders是exposedHeaders的子集
                .maxAge(18000L); // 预检请求OPTIONS请求的缓存时间，在这个时间段里，对于相同的跨域请求不会再预检了
    }

}
