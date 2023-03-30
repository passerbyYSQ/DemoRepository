package top.ysqorz.redis.controller;

import cn.hutool.core.util.RandomUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ysqorz.redis.lock.ReentrantRedisLock;
import top.ysqorz.redis.lock.RedisLockFactory;
import top.ysqorz.redis.lock.WatchDogExecutor;

import javax.annotation.Resource;

@RequestMapping("/test")
@RestController
public class RedisController {
    @Resource
    private RedisLockFactory redisLockFactory;

    @GetMapping("/testWatchDog")
    public void testWatchDog() throws Exception {
        ReentrantRedisLock redisLock = redisLockFactory.createRedisLock("testWatchDog");
        try {
            redisLock.lock();
            System.out.println("【获取】：" + Thread.currentThread().getName());
            long startTime = System.currentTimeMillis();
            Thread.sleep(9000); // 模拟业务处理
            System.out.println("耗时：" + (System.currentTimeMillis() - startTime) + " ms");
        } finally {
            redisLock.unlock();
            System.out.println("【释放】：" + Thread.currentThread().getName());
        }
    }

    @GetMapping("/testConcurrency")
    public void testConcurrency() throws Exception {
        ReentrantRedisLock redisLock = redisLockFactory.createRedisLock("testConcurrency");
        try {
            redisLock.lock();
            System.out.println("【获取】：" + Thread.currentThread().getName());
            long startTime = System.currentTimeMillis();
            Thread.sleep(RandomUtil.randomInt(2000));
            System.out.println("耗时：" + (System.currentTimeMillis() - startTime) + " ms");
        } finally {
            System.out.println("【释放】：" + Thread.currentThread().getName());
            redisLock.unlock();
        }
    }

    @GetMapping("/testReentrant")
    public void testReentrant() {
        ReentrantRedisLock redisLock = redisLockFactory.createRedisLock("testReentrant");
        try {
            // 重入次数 1
            redisLock.lock();
            System.out.println(WatchDogExecutor.getReentrantCount(redisLock.getLockIdentifier())); // 1

            try {
                // 重入次数 2
                redisLock.lock();
                System.out.println(WatchDogExecutor.getReentrantCount(redisLock.getLockIdentifier())); // 2

                try {
                    // 重入次数 3
                    redisLock.lock();
                    System.out.println(WatchDogExecutor.getReentrantCount(redisLock.getLockIdentifier())); // 3

                } finally {
                    redisLock.unlock();
                    System.out.println(WatchDogExecutor.getReentrantCount(redisLock.getLockIdentifier())); // 2
                }


            } finally {
                redisLock.unlock();
                System.out.println(WatchDogExecutor.getReentrantCount(redisLock.getLockIdentifier())); // 1
            }

        } finally {
            redisLock.unlock();
            System.out.println(WatchDogExecutor.getReentrantCount(redisLock.getLockIdentifier())); // -1 计数被清除了
        }
    }
}
