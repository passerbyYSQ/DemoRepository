package top.ysqorz.TransmittableThreadLocal.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;

import java.util.concurrent.*;

@Configuration
public class RequestContextConfig {
    @Bean
    public static RequestContextFilter requestContextFilter() {
        return new MyRequestContextFilter(true);
    }

    /**
     * 测试用的线程池
     */
    @Bean
    public ExecutorService testExecutorService() {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        threadPoolExecutor.allowCoreThreadTimeOut(false);
        return TtlExecutors.getTtlExecutorService(threadPoolExecutor);
    }
}
