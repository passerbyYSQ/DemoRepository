package top.ysqorz.redis.lock;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode // 必须重写equal和hashCode方法
@AllArgsConstructor
@Getter
public class RenewExpirationTaskContext {
    private Thread workerThread;
    private String lockKey;
    private String threadIdentifier;
    @EqualsAndHashCode.Exclude
    private Long lockDuration; // 锁的有效期
    @EqualsAndHashCode.Exclude
    private Long lockExpireTime; // TODO 跟实际过期时间的微小误差

    public void setLockExpireTime(Long lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }
}
