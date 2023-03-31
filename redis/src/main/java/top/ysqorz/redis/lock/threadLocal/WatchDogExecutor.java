package top.ysqorz.redis.lock.threadLocal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.ysqorz.redis.lock.RenewExpirationTaskContext;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WatchDogExecutor {
    private static StringRedisTemplate redisTemplate;

    private static final Queue<RenewExpirationTaskContext> taskQueue = new ConcurrentLinkedQueue<>();
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1,
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true); // 设置守护线程
                return thread;
            });
    /**
     * 每个线程自己重入的锁
     */
    private static final ThreadLocal<Map<String, Integer>> reentrantLocal = ThreadLocal.withInitial(HashMap::new);
    public static final Long watchInterval = Duration.ofSeconds(2).toMillis(); // 看护的时间间隔

    @Autowired
    public WatchDogExecutor(StringRedisTemplate redisTemplate) {
        WatchDogExecutor.redisTemplate = redisTemplate;
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

    public static boolean isReentrant(String lockKey) {
        return reentrantLocal.get().getOrDefault(lockKey, 0) > 0;
    }

    public static int increaseReentrantCount(String lockKey) {
        Map<String, Integer> reentrantMap = reentrantLocal.get();
        int count = reentrantMap.getOrDefault(lockKey, 0) + 1;
        reentrantMap.put(lockKey, count);
        return count;
    }

    public static int decreaseReentrantCount(String lockKey) {
        Map<String, Integer> reentrantMap = reentrantLocal.get();
        int remainedCount = reentrantMap.getOrDefault(lockKey, 0) - 1;
        if (remainedCount > 0) {
            reentrantMap.put(lockKey, remainedCount);
        } else {
            reentrantMap.remove(lockKey);
        }
        return remainedCount;
    }

    public static int getReentrantCount(String lockKey) {
        return reentrantLocal.get().getOrDefault(lockKey, 0);
    }

    /**
     * 可用于请求结束后清空，防止内存泄露
     */
    public static void clearReentrantLocal() {
        reentrantLocal.remove();
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
                        continue;
                    }
                    // 尝试续期，将锁的过期时间重置为一个有效周期之后
                    Boolean expiredSucceed = redisTemplate.expire(taskContext.getLockKey(), taskContext.getLockDuration(), TimeUnit.MILLISECONDS);
                    // 续期失败，有可能是Redis上的锁实际上已经过期不存在了
                    if (Boolean.TRUE.equals(expiredSucceed)) {
                        // 续期之后更新过期时间
                        taskContext.setLockExpireTime(System.currentTimeMillis() + taskContext.getLockDuration());
                        log.info("续期成功，lockKey: {}", taskContext.getLockKey());
                    } else {
                        iterator.remove();
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
