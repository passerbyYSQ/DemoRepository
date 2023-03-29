package top.ysqorz.redis.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

@Component
public class RedisLockFactory {
    // 先通过bean的名字(不配置name参数，则变量名即为bean的名字)找，再通过bean的类型找。如果通过名字找到了，则不通过类型找了，类型不对直接报错
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    public RedisLock createRedisLock(String businessKey, Duration duration) {
        return new RedisLock(redisTemplate, businessKey, duration);
    }

    public RedisLock createRedisLock(String businessKey) {
        return new RedisLock(redisTemplate, businessKey, Duration.ofSeconds(5)); // 比看门狗看护间隔大。第二次看护会续期
    }
}
