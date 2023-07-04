package top.ysqorz.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Hello world!
 */
@SpringBootApplication
public class OSSApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(OSSApplication.class, args);

//        IOSSStrategy strategy = context.getBean(IOSSStrategy.class);
//        System.out.println(strategy.generateSharedURL("/demo/test.txt", Duration.ofMinutes(1)));
    }
}
