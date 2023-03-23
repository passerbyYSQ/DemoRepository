package top.ysqorz.r2dbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * <a href="https://juejin.cn/post/7205412097954922557">...</a>
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class R2dbcApp {
    public static void main(String[] args) {
        SpringApplication.run(R2dbcApp.class, args);
    }
}
