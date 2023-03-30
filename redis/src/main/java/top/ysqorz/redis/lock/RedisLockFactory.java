package top.ysqorz.redis.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.ysqorz.redis.lock.lua.ReentrantRedisLock1;
import top.ysqorz.redis.lock.threadLocal.ReentrantRedisLock;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.time.Duration;

@Component
public class RedisLockFactory {
    // 先通过bean的名字(不配置name参数，则变量名即为bean的名字)找，再通过bean的类型找。如果通过名字找到了，则不通过类型找了，类型不对直接报错
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public ReentrantRedisLock createRedisLock(String businessKey, Duration duration) {
        return new ReentrantRedisLock(stringRedisTemplate, businessKey, duration);
    }

    public ReentrantRedisLock createRedisLock(String businessKey) {
        return new ReentrantRedisLock(stringRedisTemplate, businessKey, Duration.ofSeconds(5)); // 比看门狗看护间隔大。第二次看护会续期
    }

    public ReentrantRedisLock1 createRedisLock1(String businessKey, Duration duration) {
        return new ReentrantRedisLock1(redisTemplate, businessKey, duration);
    }

    public ReentrantRedisLock1 createRedisLock1(String businessKey) {
        return new ReentrantRedisLock1(redisTemplate, businessKey, Duration.ofSeconds(5));
    }

    /**
     * 为分布式锁生成标识符：JVM进程名称_线程ID_线程名称
     */
    public static String generateThreadIdentifier() {
        return ManagementFactory.getRuntimeMXBean().getName() + "_" + Thread.currentThread().getId() + "_" +Thread.currentThread().getName();
    }
}
