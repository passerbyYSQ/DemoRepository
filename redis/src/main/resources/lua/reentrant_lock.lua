local lockKey = KEYS[1]
local threadIdentifier = KEYS[2]
local expirationMillis = ARGV[1]
-- 获取这把锁的持有者是哪个线程
local tid = redis.call('GET', lockKey)
-- 如果不存在，说明锁还没被抢占
if not tid then
    -- 成功抢到锁，占领锁的坑位
    redis.call('SET', lockKey, threadIdentifier, 'PX', expirationMillis)
    -- 同时设置重入次数为1，注意一个线程在执行过程中可能重入多把不同的锁
    redis.call('SET', threadIdentifier..':'..lockKey, 1, 'PX', expirationMillis)
    return true
else
    -- 如果锁的持有者是当前线程，则直接重入
    if tid == threadIdentifier then
        redis.call('INCR', threadIdentifier..':'..lockKey)
        return true
    end
        return false
end