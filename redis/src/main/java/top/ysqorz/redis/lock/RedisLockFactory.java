package top.ysqorz.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.ysqorz.redis.lock.threadLocal.ReentrantRedisLock;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.time.Duration;

@Component
public class RedisLockFactory {
    // 先通过bean的名字(不配置name参数，则变量名即为bean的名字)找，再通过bean的类型找。如果通过名字找到了，则不通过类型找了，类型不对直接报错
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    public ReentrantRedisLock createRedisLock(String businessKey, Duration duration) {
        return new ReentrantRedisLock(redisTemplate, businessKey, duration);
    }

    public ReentrantRedisLock createRedisLock(String businessKey) {
        return new ReentrantRedisLock(redisTemplate, businessKey, Duration.ofSeconds(5)); // 比看门狗看护间隔大。第二次看护会续期
    }

    /**
     * 为分布式锁生成标识符：JVM进程名称_线程ID_线程名称
     */
    public static String generateThreadIdentifier() {
        return ManagementFactory.getRuntimeMXBean().getName() + "_" + Thread.currentThread().getId() + "_" +Thread.currentThread().getName();
    }
}
