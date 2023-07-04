package top.ysqorz.redis.lock.lua;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import top.ysqorz.redis.lock.IReentrantRedisLock;
import top.ysqorz.redis.lock.RedisLockFactory;
import top.ysqorz.redis.lock.RenewExpirationTaskContext;
import top.ysqorz.redis.lock.threadLocal.WatchDogExecutor;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

@Getter
public class ReentrantRedisLock1 implements IReentrantRedisLock {
    public static final String REDIS_LOCK_KEY = "ReentrantRedisLock1:"; // :用于key分组
    private StringRedisTemplate redisTemplate;
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

    public ReentrantRedisLock1(StringRedisTemplate redisTemplate, String businessKey, Duration duration) {
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
    public int getReentrantCount() {
        String count = redisTemplate.opsForValue().get(threadIdentifier + ":" + lockKey);
        return count == null ? 0 : Integer.parseInt(count);
    }

    @Override
    public void unlock() {
        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>(REENTRANT_UNLOCK_LUA, Long.class);
        Long reentrantCount = redisTemplate.execute(luaScript, Arrays.asList(lockKey, threadIdentifier));
        // 锁的持有者是当前线程，且是最后一次释放锁，从队列中移除看护任务
        if (reentrantCount != null && reentrantCount == 0) {
            WatchDogExecutor1.removeTask(taskContext);
        }
    }

    public boolean tryLock(Duration timout, int tryCount) {
        long startTime = System.currentTimeMillis();
        long timoutMillis = timout == null ? Long.MAX_VALUE : timout.toMillis(); // timout为null则超时无限制
        int totalTryCount = tryCount <= 0 ? Integer.MAX_VALUE : tryCount; // tryCount为负数则无限制
        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>(REENTRANT_LOCK_LUA, Long.class);
        Function<Integer, Long> tryLockByLua = triedCount -> {
            // 超时
            if (System.currentTimeMillis() - startTime > timoutMillis) {
                return Long.valueOf(-1);
            }
            // 超出重试次数
            if (triedCount >= totalTryCount) {
                return Long.valueOf(-1);
            }
            // 锁已经被其他线程抢占了
            Long reentrantCount = redisTemplate.execute(luaScript, Arrays.asList(lockKey, threadIdentifier), String.valueOf(lockDuration));
            if (reentrantCount == null || reentrantCount < 0) {
                return Long.valueOf(-1);
            }
            // 当前线程成功抢占锁，返回重入次数
            return reentrantCount;
        };
        Long reentrantCount;
        for (int triedCount = 0;
             (reentrantCount = tryLockByLua.apply(triedCount)) < 0;
             triedCount++) {
            try {
                //noinspection BusyWait
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        // 第一次成功抢占锁，将守护任务放入队列
        if (reentrantCount == 1) {
            taskContext = new RenewExpirationTaskContext(Thread.currentThread(), lockKey, threadIdentifier,
                    lockDuration, System.currentTimeMillis() + lockDuration);
            WatchDogExecutor1.pushTask(taskContext);
        }
        return true;
        // 此时当前线程已经获取到lockKey这把锁，换言之不可能存在其他线程并发添加这把锁的TaskContext，只可能是自己在重入这把锁的时候添加重复的TaskContext
        // 所以这里的contain和push不需要再做同步处理
//        if (!WatchDogExecutor1.containTask(taskContext)) {
//            // 上锁成功，委托给看门狗看护
//            WatchDogExecutor1.pushTask(taskContext);
//        }
    }
}
