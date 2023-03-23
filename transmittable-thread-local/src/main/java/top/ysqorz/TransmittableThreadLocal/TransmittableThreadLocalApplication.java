package top.ysqorz.TransmittableThreadLocal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Hello world!
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TransmittableThreadLocalApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransmittableThreadLocalApplication.class, args);
    }
}
