package com.dev.coupon.coupon.service;

import com.dev.coupon.coupon.domain.CouponIssueResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisIssueService {

	private final StringRedisTemplate redisTemplate;

	private static final String RESERVE_COUPON_LUA_SCRIPT = """
		if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
			return 1
		end
	
		local stock = tonumber(redis.call('GET', KEYS[1]))
		if stock == nil or stock <= 0 then
			return 2
		end
	
		redis.call('DECR', KEYS[1])
		redis.call('SADD', KEYS[2], ARGV[1])
		return 3
	""";

	private static final RedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
		RESERVE_COUPON_LUA_SCRIPT,
		Long.class
	);

	public CouponIssueResult reserveCoupon(Long eventId, Long userId) {
		Long result = redisTemplate.execute(
				  RESERVE_SCRIPT,
				  List.of(stockKey(eventId), issuedUsersKey(eventId)),
				  userId.toString()
		);

		if (result == 1L) {
			return CouponIssueResult.ALREADY_ISSUED;
		}

		if (result == 2L) {
			return CouponIssueResult.SOLD_OUT;
		}

		if (result == 3L) {
			return CouponIssueResult.SUCCESS;
		}

		return CouponIssueResult.SOLD_OUT;
	}

	public void initEventStock(Long eventId, int remainingQuantity) {
		redisTemplate.opsForValue().set(stockKey(eventId), String.valueOf(remainingQuantity));
	}

	private String stockKey(Long eventId) {
		return "coupon:event:" + eventId + ":stock";
	};

	private String issuedUsersKey(Long eventId) {
		return "coupon:event:" + eventId + ":issued-users";
	}
}
