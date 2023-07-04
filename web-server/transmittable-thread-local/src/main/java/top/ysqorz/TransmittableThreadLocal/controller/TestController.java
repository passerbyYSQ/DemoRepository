package top.ysqorz.TransmittableThreadLocal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.ysqorz.TransmittableThreadLocal.config.MyRequestContextHolder;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private ExecutorService testExecutorService;

    @GetMapping("/demo")
    public void test() throws InterruptedException {
        // 将RequestAttributes对象设置为子线程共享
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(sra, true);
        tryGet("主线程：");
        testExecutorService.execute(() -> {
            tryGet("线程池线程：");
        });
        Thread.sleep(1000 * 60 * 5);
        new Thread(() -> tryGet("子线程：")).start();
    }

    @GetMapping("/demo1")
    public void test1() {
        // 将RequestAttributes对象设置为子线程共享
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(sra, true);
        testExecutorService.execute(() -> {
            tryGet("线程池线程：");
        });
    }

    public void tryGet(String msg) {
        RequestAttributes requestAttrs = (ServletRequestAttributes) MyRequestContextHolder.getRequestAttributes();
        RequestAttributes requestAttrs1 = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        System.out.print(msg);
        System.out.println(requestAttrs1);
    }
}
