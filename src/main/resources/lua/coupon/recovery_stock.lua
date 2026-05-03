local stockKey = KEYS[1]
local issuedUsersKey = KEYS[2]
local issueStartAtKey = KEYS[3]
local issueEndAtKey = KEYS[4]

local remainingQuantity = ARGV[1]
local issueStartAt = ARGV[2]
local issueEndAt = ARGV[3]

redis.call('DEL', stockKey, issuedUsersKey, issueStartAtKey, issueEndAtKey)
redis.call('SET', stockKey, remainingQuantity)
redis.call('SET', issueStartAtKey, issueStartAt)
redis.call('SET', issueEndAtKey, issueEndAt)

for i = 4, #ARGV do
	redis.call('SADD', issuedUsersKey, ARGV[i])
end

return true
