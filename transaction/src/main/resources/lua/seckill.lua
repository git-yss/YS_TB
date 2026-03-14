-- 秒杀原子操作Lua脚本
-- KEYS[1]: 商品库存key (seckill:stock)
-- KEYS[2]: 用户订单key (seckill:user:order:userId_itemId)
-- KEYS[3]: 用户购买记录key (seckill:user:record:userId)
-- ARGV[1]: 商品ID
-- ARGV[2]: 用户ID
-- ARGV[3]: 订单过期时间（分钟）
-- ARGV[4]: 商品JSON数据

local stockKey = KEYS[1]
local userOrderKey = KEYS[2]
local userRecordKey = KEYS[3]
local itemId = ARGV[1]
local userId = ARGV[2]
local orderTimeOut = tonumber(ARGV[3])
local itemJson = ARGV[4]

-- 检查秒杀活动是否存在
if not redis.call('EXISTS', stockKey) then
    return redis.error_reply('秒杀活动已结束')
end

-- 检查用户是否已经秒杀过该商品
if redis.call('HEXISTS', userOrderKey, itemId) == 1 then
    return redis.error_reply('您已经秒杀过该商品')
end

-- 获取商品信息
local goodsJson = redis.call('HGET', stockKey, itemId)
if not goodsJson then
    return redis.error_reply('商品不存在')
end

-- 解析JSON获取库存数量（简化处理，实际应该使用JSON解析库）
-- 这里假设JSON格式包含库存信息，需要反序列化
-- 由于Lua不支持直接JSON解析，这里使用Redis的JSON命令或返回JSON让应用层解析

-- 为了原子性，这里我们使用Redis的HINCRBY来原子递减库存
-- 假设我们用一个单独的key存储库存计数
local stockCountKey = stockKey .. ':count:' .. itemId

-- 检查并原子递减库存
local stock = tonumber(redis.call('GET', stockCountKey))
if not stock then
    -- 第一次初始化库存（从商品JSON中解析或从参数传入）
    stock = tonumber(redis.call('HGET', stockKey .. ':stock', itemId) or '0')
    if stock == nil or stock <= 0 then
        return redis.error_reply('商品已售罄')
    end
    redis.call('SET', stockCountKey, stock)
end

-- 原子递减库存
local newStock = tonumber(redis.call('DECR', stockCountKey))
if newStock < 0 then
    -- 库存不足，回滚
    redis.call('INCR', stockCountKey)
    return redis.error_reply('商品已售罄')
end

-- 更新商品JSON中的库存（简化处理）
redis.call('HSET', stockKey, itemId, itemJson)

-- 设置用户购买记录
redis.call('SADD', userRecordKey, itemId)
redis.call('EXPIRE', userRecordKey, orderTimeOut * 60)

-- 设置用户订单
redis.call('HSET', userOrderKey, itemId, itemJson)
redis.call('EXPIRE', userOrderKey, orderTimeOut * 60)

return redis.status_reply('OK')
