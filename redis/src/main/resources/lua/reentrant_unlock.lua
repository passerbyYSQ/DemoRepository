local lockKey = KEYS[1]
local threadIdentifier = KEYS[2]
-- 获取这把锁的持有者是哪个线程
local tid = redis.call('GET', lockKey)
if tid and tid == threadIdentifier then
    redis.call('DEL', lockKey)
    redis.call('DEL', threadIdentifier..':'.. lockKey)
    return true
else
    return false
end