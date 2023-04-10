local lockKey = KEYS[1]
local threadIdentifier = KEYS[2]
local expirationMillis = tonumber(ARGV[1])
-- 需要判断是否还存在，只有存在才续签
-- 而key不存在时，PEXPIRE会忽略并返回0，不会重新生成key
return redis.call('PEXPIRE', lockKey, expirationMillis) == 1 and
    redis.call('PEXPIRE', threadIdentifier..':'..lockKey, expirationMillis) == 1