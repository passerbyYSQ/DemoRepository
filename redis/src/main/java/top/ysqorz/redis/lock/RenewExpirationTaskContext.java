package top.ysqorz.redis.lock;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@AllArgsConstructor
@Data
public class RenewExpirationTaskContext {
    private Thread workerThread;
    private String lockKey;
    private Duration lockDuration; // 锁的有效期
    private Long lockExpireTime;
}
