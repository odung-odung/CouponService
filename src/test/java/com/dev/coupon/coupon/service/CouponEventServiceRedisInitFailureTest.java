package com.dev.coupon.coupon.service;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Rollback(value = false)
class CouponEventServiceRedisInitFailureTest {

	@Autowired
	private CouponEventService couponEventService;

	@Autowired
	private CouponEventRepository eventRepository;

	@Test
	@DisplayName("Redis 발급 상태 초기화가 실패하면 재동기화 대기 상태로 마킹")
	void createMarkPendingWhenRedisInitFailed() {
		LocalDateTime issueStartAt = LocalDateTime.now().plusMinutes(10);
		LocalDateTime issueEndAt = issueStartAt.plusDays(1);

		CouponEventResponse response = couponEventService.create(createRequest(
				  "test coupon " + UUID.randomUUID(),
				  100,
				  issueStartAt,
				  issueEndAt
		));

		CouponEvent savedEvent = eventRepository.findById(response.getId()).orElseThrow();
		assertThat(savedEvent.isStockResyncPending()).isTrue();
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

	@TestConfiguration
	static class RedisInitFailureConfig {

		@Bean
		@Primary
		RedisIssueService failingRedisIssueService(
				  StringRedisTemplate redisTemplate,
				  CouponStockResyncService resyncService
		) {
			return new RedisIssueService(redisTemplate, resyncService) {
				@Override
				public void initEventIssueState(
						  Long eventId,
						  int remainingQuantity,
						  LocalDateTime issueStartAt,
						  LocalDateTime issueEndAt
				) {
					throw new RuntimeException("Redis init failed");
				}
			};
		}
	}
}
