package top.ysqorz.redis.lock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

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
    // TODO daemon Thread
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
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

    public static boolean isReentrant(String lockUUID) {
        return reentrantLocal.get().getOrDefault(lockUUID, 0) > 0;
    }

    public static void increaseReentrantCount(String lockUUID) {
        Map<String, Integer> reentrantMap = reentrantLocal.get();
        reentrantMap.put(lockUUID, reentrantMap.getOrDefault(lockUUID, 0) + 1);
    }

    public static void decreaseReentrantCount(String lockUUID) {
        Map<String, Integer> reentrantMap = reentrantLocal.get();
        int remainedCount = reentrantMap.getOrDefault(lockUUID, 0) - 1;
        if (remainedCount > 0) {
            reentrantMap.put(lockUUID, remainedCount);
        } else {
            reentrantMap.remove(lockUUID);
        }
    }

    public static int getReentrantCount(String lockUUID) {
        return reentrantLocal.get().getOrDefault(lockUUID, -1);
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
                    Boolean expiredSucceed = redisTemplate.expire(taskContext.getLockKey(), taskContext.getLockDuration());
                    // 续期失败，有可能是Redis上的锁实际上已经过期不存在了
                    if (!Boolean.TRUE.equals(expiredSucceed)) {
                        iterator.remove();
                    } else {
                        log.info("续期成功，lockKey: {}", taskContext.getLockKey());
                    }
                }
            } catch (Exception ex) {
                // 直接包裹整块处理代码，防止单次处理异常导致看门狗线程挂掉
                if (taskContext != null) {
                    taskContext.getWorkerThread().interrupt(); // 设置工作线程的中断标记
                }
            }
        }
    }
}
