package top.ysqorz.redis.lock.lua;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import top.ysqorz.redis.lock.IReentrantRedisLock;
import top.ysqorz.redis.lock.RedisLockFactory;
import top.ysqorz.redis.lock.RenewExpirationTaskContext;
import top.ysqorz.redis.lock.threadLocal.WatchDogExecutor;

import java.time.Duration;
import java.util.Arrays;

public class ReentrantRedisLock1 implements IReentrantRedisLock {
    public static final String REDIS_LOCK_KEY = "ReentrantRedisLock1:"; // :用于key分组
    private RedisTemplate<String, Object> redisTemplate;
    private String lockKey;
    private String threadIdentifier;
    private long lockDuration;
    private RenewExpirationTaskContext taskContext;
    public static final String REENTRANT_LOCK_LUA;
    public static final String REENTRANT_UNLOCK_LUA;

    static {
        REENTRANT_LOCK_LUA = FileUtil.readUtf8String(new ClassPathResource("lua/reentrant_lock.lua").getFile());
        REENTRANT_UNLOCK_LUA = FileUtil.readUtf8String(new ClassPathResource("lua/reentrant_unlock.lua").getFile());
    }

    public ReentrantRedisLock1(RedisTemplate<String, Object> redisTemplate, String businessKey, Duration duration) {
        if (duration == null || duration.toMillis() < WatchDogExecutor.watchInterval) {
            throw new RuntimeException("锁的有效期不能小于看门狗看护的时间间隔");
        }
        this.redisTemplate = redisTemplate;
        this.lockDuration = duration.toMillis(); // 锁的有效期
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

    @Override
    public void unlock() {
        DefaultRedisScript<Boolean> luaScript = new DefaultRedisScript<>(REENTRANT_UNLOCK_LUA, Boolean.class);
        redisTemplate.execute(luaScript, Arrays.asList(lockKey, threadIdentifier));
        if (taskContext != null) {
            WatchDogExecutor1.removeTask(taskContext);
        }
    }

    public boolean tryLock(Duration timout, int tryCount) {
        long startTime = System.currentTimeMillis();
        long timoutMillis = timout == null ? Long.MAX_VALUE : timout.toMillis(); // timout为null则超时无限制
        int totalTryCount = tryCount <= 0 ? Integer.MAX_VALUE : tryCount; // tryCount为负数则无限制
        DefaultRedisScript<Boolean> luaScript = new DefaultRedisScript<>(REENTRANT_LOCK_LUA, Boolean.class);
        Boolean lockSucceed = Boolean.FALSE;
        for (int triedCount = 0;
             (System.currentTimeMillis() - startTime) < timoutMillis
                     && (triedCount < totalTryCount)
                     // 注意lockSucceed有可能为null，此时认为上锁失败
                     && !Boolean.TRUE.equals(lockSucceed = redisTemplate.execute(luaScript, Arrays.asList(lockKey, threadIdentifier), lockDuration));
             triedCount++) {
            Thread.yield();
        }
        if (!Boolean.TRUE.equals(lockSucceed)) {
            return false;
        }
        // 上锁成功，委托给看门狗看护
        taskContext = new RenewExpirationTaskContext(Thread.currentThread(), lockKey, threadIdentifier,
                lockDuration, System.currentTimeMillis() + lockDuration);
        WatchDogExecutor1.pushTask(taskContext);
        return true;
    }
}
