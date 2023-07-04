package top.ysqorz.redis.lock.lua;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import top.ysqorz.redis.lock.RenewExpirationTaskContext;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@Component
public class WatchDogExecutor1 {
    private static StringRedisTemplate redisTemplate;

    private static final Queue<RenewExpirationTaskContext> taskQueue = new ConcurrentLinkedQueue<>();
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1,
            runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true); // 设置守护线程
        return thread;
    });
    public static final Long watchInterval = Duration.ofSeconds(2).toMillis(); // 看护的时间间隔
    public static final String REENTRANT_RENEWAL_LUA;

    static {
        REENTRANT_RENEWAL_LUA = FileUtil.readUtf8String(new ClassPathResource("lua/reentrant_renewal.lua").getFile());
    }

    @Autowired
    public WatchDogExecutor1(StringRedisTemplate redisTemplate) {
        WatchDogExecutor1.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        scheduledExecutor.scheduleAtFixedRate(new RenewExpirationTask(), 0, watchInterval, TimeUnit.MILLISECONDS);
    }

    public static void pushTask(RenewExpirationTaskContext taskContext) {
        taskQueue.offer(taskContext);
    }

    public static void removeTask(RenewExpirationTaskContext taskContext) {
        taskQueue.remove(taskContext);
    }

    public static boolean containTask(RenewExpirationTaskContext taskContext) {
        return taskQueue.contains(taskContext);
    }

    @AllArgsConstructor
    public static class RenewExpirationTask implements Runnable {
        @Override
        public void run() {
            RenewExpirationTaskContext taskContext = null;
            try {
                if (taskQueue.isEmpty()) {
                    return;
                }
                log.info("任务队列长度：{}", taskQueue.size());
                Iterator<RenewExpirationTaskContext> iterator = taskQueue.iterator();
                // 扫描队列，逐个检查是否续期
                while (iterator.hasNext()) {
                    taskContext = iterator.next();
                    // 无效任务，直接移除
                    if (taskContext == null) {
                        iterator.remove();
                        continue;
                    }
                    // 距离过期时间已经不足一个watchInterval，才续期，否则时间还充裕，不需要续期
                    long remainedMillis = taskContext.getLockExpireTime() - System.currentTimeMillis();
                    if (remainedMillis >= watchInterval) {
                        log.info("距离过期时间大于一个watchInterval，不续期，lockKey: {}", taskContext.getLockKey());
                        continue;
                    }
                    // 尝试续期，将锁和重入次数的过期时间重置为一个有效周期之后
                    DefaultRedisScript<Boolean> luaScript = new DefaultRedisScript<>(REENTRANT_RENEWAL_LUA, Boolean.class);
                    Boolean expiredSucceed = redisTemplate.execute(luaScript, Arrays.asList(taskContext.getLockKey(),
                            taskContext.getThreadIdentifier()), String.valueOf(taskContext.getLockDuration()));
                    // 续期失败，有可能是Redis上的锁实际上已经过期不存在了
                    if (Boolean.TRUE.equals(expiredSucceed)) {
                        // 续期之后更新过期时间
                        taskContext.setLockExpireTime(System.currentTimeMillis() + taskContext.getLockDuration());
                        log.info("续期成功，lockKey: {}", taskContext.getLockKey());
                    } else {
                        iterator.remove();
                        log.info("续期失败，移除任务，lockKey: {}", taskContext.getLockKey());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // 直接包裹整块处理代码，防止单次处理异常导致看门狗线程挂掉
                if (taskContext != null) {
                    taskContext.getWorkerThread().interrupt(); // 设置工作线程的中断标记
                }
            }
        }
    }
}
