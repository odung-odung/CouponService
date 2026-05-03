package com.dev.coupon.coupon.service;

import com.dev.coupon.common.exception.SystemException;
import com.dev.coupon.common.util.CouponRedisKey;
import com.dev.coupon.common.util.RedisLuaScriptLoader;
import com.dev.coupon.coupon.exception.SystemErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisRecoveryService {

	private final StringRedisTemplate redisTemplate;

	private static final RedisScript<Boolean> RECOVERY_SCRIPT =
			  RedisLuaScriptLoader.booleanScript("lua/coupon/recovery_stock.lua");

	public void restoreStock(Long eventId,
									 List<Long> userIds,
									 int remainingQuantity,
									 LocalDateTime issueStartAt,
									 LocalDateTime issueEndAt
	) {
		// ARGV[1] = remainingQuantity, ARGV[2] = issueStartAt, ARGV[3] = issueEndAt
		// ARGV[4] 첫 번째 userId, ARGV[5] = 두 번째 userId ...
		long startAt = toEpochMillis(issueStartAt);
		long endAt = toEpochMillis(issueEndAt);
		List<String> args = new ArrayList<>();

		args.add(String.valueOf(remainingQuantity));
		args.add(String.valueOf(startAt));
		args.add(String.valueOf(endAt));

		userIds.stream()
				  .map(String::valueOf)
				  .forEach(args::add);

		try {
			Boolean result = redisTemplate.execute(
					  RECOVERY_SCRIPT,
					  List.of(
								 CouponRedisKey.stock(eventId),
								 CouponRedisKey.issuedUsers(eventId),
								 CouponRedisKey.issueStartAt(eventId),
								 CouponRedisKey.issueEndAt(eventId)
					  ),
					  args.toArray()
			);

			if (!Boolean.TRUE.equals(result)) {
				log.error("[REDIS_RECOVERY_INVALID_RESULT] result={}, eventId={}", result, eventId);
				throw new SystemException(SystemErrorCode.REDIS_RECOVERY_EXECUTION_FAILED);
			}
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			log.error("[REDIS_RECOVERY_EXECUTION_FAILED] eventId={}, remainingQuantity={}", eventId, remainingQuantity, e);
			throw new SystemException(SystemErrorCode.REDIS_RECOVERY_EXECUTION_FAILED, e);
		}
	}

	private long toEpochMillis(LocalDateTime dateTime) {
		return dateTime.atZone(ZoneId.of("Asia/Seoul"))
				  .toInstant()
				  .toEpochMilli();
	}
}
