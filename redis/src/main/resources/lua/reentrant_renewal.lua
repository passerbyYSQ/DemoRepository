local lockKey = KEYS[1]
local threadIdentifier = KEYS[2]
local expirationMillis = tonumber(ARGV[1])
return redis.call('PEXPIRE', lockKey, expirationMillis) == 1 and
    redis.call('PEXPIRE', threadIdentifier..':'..lockKey, expirationMillis) == 1