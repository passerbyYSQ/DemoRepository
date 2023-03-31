package top.ysqorz.redis.lock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RenewExpirationTaskContext {
    private Thread workerThread;
    private String lockKey;
    private String threadIdentifier;
    private Long lockDuration; // 锁的有效期
    private Long lockExpireTime;
}
