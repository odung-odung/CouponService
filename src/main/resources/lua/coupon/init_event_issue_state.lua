local stockKey = KEYS[1]
local issueStartAtKey = KEYS[2]
local issueEndAtKey = KEYS[3]

if redis.call('EXISTS', stockKey) == 1
    or redis.call('EXISTS', issueStartAtKey) == 1
    or redis.call('EXISTS', issueEndAtKey) == 1 then
    return false
end

redis.call('SET', stockKey, ARGV[1])
redis.call('SET', issueStartAtKey, ARGV[2])
redis.call('SET', issueEndAtKey, ARGV[3])

return true
