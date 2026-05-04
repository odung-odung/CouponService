package com.dev.coupon.coupon.service;

import com.dev.coupon.common.util.CouponRedisKey;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import com.dev.coupon.coupon.dto.CouponEventCreateRequest;
import com.dev.coupon.coupon.dto.CouponEventResponse;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Rollback(value = false)
class CouponEventServiceTest {

	@Autowired
	private CouponEventService couponEventService;

	@Autowired
	private CouponEventRepository eventRepository;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	@DisplayName("쿠폰 이벤트 생성 시 DB에 저장하고 Redis 발급 상태는 초기화")
	void create() {
		LocalDateTime issueStartAt = LocalDateTime.now().plusMinutes(10);
		LocalDateTime issueEndAt = issueStartAt.plusDays(1);

		CouponEventResponse response = couponEventService.create(createRequest(
				  "test coupon " + UUID.randomUUID(),
				  100,
				  issueStartAt,
				  issueEndAt
		));

		CouponEvent savedEvent = eventRepository.findById(response.getId()).orElseThrow();

		assertThat(savedEvent.getRemainingQuantity()).isEqualTo(100);
		assertThat(savedEvent.isStockResyncPending()).isFalse();
		assertThat(redisTemplate.opsForValue().get(CouponRedisKey.stock(response.getId()))).isEqualTo("100");
		assertThat(redisTemplate.opsForValue().get(CouponRedisKey.issueStartAt(response.getId())))
				  .isEqualTo(String.valueOf(toEpochMillis(issueStartAt)));
		assertThat(redisTemplate.opsForValue().get(CouponRedisKey.issueEndAt(response.getId())))
				  .isEqualTo(String.valueOf(toEpochMillis(issueEndAt)));
		assertThat(redisTemplate.hasKey(CouponRedisKey.issuedUsers(response.getId()))).isFalse();
	}

	private CouponEventCreateRequest createRequest(
			  String name,
			  int totalQuantity,
			  LocalDateTime issueStartAt,
			  LocalDateTime issueEndAt
	) {
		CouponEventCreateRequest request = new CouponEventCreateRequest();

		ReflectionTestUtils.setField(request, "name", name);
		ReflectionTestUtils.setField(request, "status", EventStatus.OPEN);
		ReflectionTestUtils.setField(request, "discountType", DiscountType.FIXED_AMOUNT);
		ReflectionTestUtils.setField(request, "discountValue", 1000L);
		ReflectionTestUtils.setField(request, "maxDiscountAmount", null);
		ReflectionTestUtils.setField(request, "totalQuantity", totalQuantity);
		ReflectionTestUtils.setField(request, "issueStartAt", issueStartAt);
		ReflectionTestUtils.setField(request, "issueEndAt", issueEndAt);

		return request;
	}

	private long toEpochMillis(LocalDateTime dateTime) {
		return dateTime.atZone(ZoneId.of("Asia/Seoul"))
				  .toInstant()
				  .toEpochMilli();
	}
}
