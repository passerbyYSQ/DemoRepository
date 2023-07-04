local lockKey = KEYS[1]
local threadIdentifier = KEYS[2]
local reentrantKey = threadIdentifier..':'..lockKey
-- 获取这把锁的持有者是哪个线程
local tid = redis.call('GET', lockKey)
if tid and tid == threadIdentifier then
    local reentrantCount = tonumber(redis.call('GET', reentrantKey))
    if reentrantCount and reentrantCount > 1 then
        -- 重入次数减1
        return redis.call('DECR', reentrantKey)
    else
        -- 重入次数减为0后，清除两个key
        redis.call('DEL', lockKey, reentrantKey)
        return 0
    end
else
    return -1
end