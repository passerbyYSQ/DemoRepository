package top.ysqorz.redis.lock.threadLocal;

import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import top.ysqorz.redis.lock.IReentrantRedisLock;
import top.ysqorz.redis.lock.RedisLockFactory;
import top.ysqorz.redis.lock.RenewExpirationTaskContext;

import java.time.Duration;
import java.util.Collections;

/**
 * 看门狗简要实现：https://mp.weixin.qq.com/s/ndO9prpTtGa8eAYCQAYTMA
 * 分布式锁难点细节分析：https://mp.weixin.qq.com/s/l9lcFqfXVI30qJi1r2A5-A
 * 可重入的分布式锁：https://juejin.cn/post/6844904191219990535
 */
@Getter
public class ReentrantRedisLock implements IReentrantRedisLock {
    public static final String REDIS_LOCK_KEY = "RedisLock:"; // :用于key分组
    private StringRedisTemplate redisTemplate;
    private Duration duration;
    private String lockKey;
    private String threadIdentifier;
    private RenewExpirationTaskContext taskContext;

    public ReentrantRedisLock(StringRedisTemplate redisTemplate, String businessKey, Duration duration) {
        if (duration == null || duration.toMillis() < WatchDogExecutor.watchInterval) {
            throw new RuntimeException("锁的有效期不能小于看门狗看护的时间间隔");
        }
        this.redisTemplate = redisTemplate;
        this.duration = duration; // 锁的有效期
        this.lockKey = REDIS_LOCK_KEY + businessKey; // 加上前缀以便分组
        this.threadIdentifier = RedisLockFactory.generateThreadIdentifier();
    }

    @Override
    public void lock() {
        tryLock(null, -1); // 阻塞直至获取锁成功返回
    }

    @Override
    public boolean lock(Duration timout) {
        return tryLock(timout, -1); // 不断重试获取锁，超时获取不到就返回
    }

    @Override
    public boolean tryLock() {
        return tryLock(null, 1); // 尝试获取一次，不管是否获取到都直接返回结果
    }

    @Override
    public boolean tryLock(int tryCount) {
        return tryLock(null, tryCount);
    }

    /**
     * @param timout       获取锁的超时时间
     * @param tryCount     允许重试获取锁的次数
     */
    public boolean tryLock(Duration timout, int tryCount) {
        // 如果是重入已经获得的锁，计数后直接返回
        if (WatchDogExecutor.isReentrant(lockKey)) {
            WatchDogExecutor.increaseReentrantCount(lockKey);
            return true;
        }
        long startTime = System.currentTimeMillis();
        long timoutMillis = timout == null ? Long.MAX_VALUE : timout.toMillis(); // timout为null则超时无限制
        int totalTryCount = tryCount <= 0 ? Integer.MAX_VALUE : tryCount; // tryCount为负数则无限制
        Boolean lockSucceed = Boolean.FALSE;
        for (int triedCount = 0;
                (System.currentTimeMillis() - startTime) < timoutMillis
                && (triedCount < totalTryCount)
                && !Boolean.TRUE.equals(lockSucceed = redisTemplate.opsForValue().setIfAbsent(lockKey, threadIdentifier, duration));
                triedCount++) {
            Thread.yield();
//            try {
//                //noinspection BusyWait
//                //Thread.sleep(100);
//            } catch (InterruptedException ignored) {
//            }
        }
        if (!Boolean.TRUE.equals(lockSucceed)) {
            return false;
        }
        // 上锁成功，委托给看门狗看护
        long lockExpireTime = System.currentTimeMillis() + duration.toMillis(); // 比锁在Redis上的过期时间大一点点
        taskContext = new RenewExpirationTaskContext(Thread.currentThread(), lockKey, duration, lockExpireTime);
        WatchDogExecutor.pushTask(taskContext);
        WatchDogExecutor.increaseReentrantCount(lockKey);
        return true;
    }

    @Override
    public void unlock() {
        // 自己的锁才释放
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(lockKey), threadIdentifier);
        if (taskContext != null) {
            WatchDogExecutor.removeTask(taskContext);
        }
        WatchDogExecutor.decreaseReentrantCount(lockKey);
    }
}
