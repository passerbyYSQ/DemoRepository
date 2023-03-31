package top.ysqorz.redis.controller;

import cn.hutool.core.util.RandomUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ysqorz.redis.lock.RedisLockFactory;
import top.ysqorz.redis.lock.lua.ReentrantRedisLock1;

import javax.annotation.Resource;

@RequestMapping("/test")
@RestController
public class RedisController {
    @Resource
    private RedisLockFactory redisLockFactory;

    @GetMapping("/testWatchDog")
    public void testWatchDog() throws Exception {
        ReentrantRedisLock1 redisLock = redisLockFactory.createRedisLock1("testWatchDog");
        redisLock.lock();
        try {
            System.out.println("【获取】：" + Thread.currentThread().getName());
            long startTime = System.currentTimeMillis();
            Thread.sleep(9000); // 模拟业务处理
            System.out.println("耗时：" + (System.currentTimeMillis() - startTime) + " ms");
        } finally {
            System.out.println("【释放】：" + Thread.currentThread().getName());
            redisLock.unlock();
        }
    }

    @GetMapping("/testConcurrency")
    public void testConcurrency() throws Exception {
        ReentrantRedisLock1 redisLock = redisLockFactory.createRedisLock1("testConcurrency");
        redisLock.lock();
        try {
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
    public void testReentrant() throws InterruptedException {
        ReentrantRedisLock1 redisLock = redisLockFactory.createRedisLock1("testReentrant");
        // 重入次数 1
        redisLock.lock();
        System.out.println(redisLock.getReentrantCount()); // 1
        Thread.sleep(5000);
        try {

            // 重入次数 2
            redisLock.lock();
            System.out.println(redisLock.getReentrantCount()); // 2
            Thread.sleep(5000);
            try {

                // 重入次数 3
                redisLock.lock();
                System.out.println(redisLock.getReentrantCount()); // 3
                Thread.sleep(5000);
                try {

                    System.out.println("成功重入锁");
                } finally {
                    redisLock.unlock();
                    System.out.println(redisLock.getReentrantCount()); // 2
                }


            } finally {
                redisLock.unlock();
                System.out.println(redisLock.getReentrantCount()); // 1
            }

        } finally {
            redisLock.unlock();
            System.out.println(redisLock.getReentrantCount()); // 0 被清除了
        }
    }

    @GetMapping("/dfsReentrant")
    public void dfsReentrant() {
        ReentrantRedisLock1 redisLock = redisLockFactory.createRedisLock1("testReentrant1");
        dfsReentrant(redisLock, 0, 100);
    }

    public void dfsReentrant(ReentrantRedisLock1 redisLock, int count, int total) {
        if (count >= total) {
            try {
                Thread.sleep(RandomUtil.randomInt(2000)); // 模拟完全上锁之后的业务操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            redisLock.lock();
            System.out.println("重入次数：" + redisLock.getReentrantCount());
            try {
                // 递归重入
                dfsReentrant(redisLock, count + 1, total);
            } finally {
                redisLock.unlock();
            }
        }
    }
}
