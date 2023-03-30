package top.ysqorz.redis.lock;

import java.time.Duration;

public interface IReentrantRedisLock {
    void lock();

    boolean lock(Duration timout);

    boolean tryLock();

    boolean tryLock(int tryCount);

    void unlock();
}
