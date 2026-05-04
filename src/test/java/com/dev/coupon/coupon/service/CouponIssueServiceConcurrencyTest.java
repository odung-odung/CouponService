package com.dev.coupon.coupon.service;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.common.util.CouponRedisKey;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import com.dev.coupon.coupon.repository.CouponIssueRepository;
import com.dev.coupon.user.domain.User;
import com.dev.coupon.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CouponIssueServiceConcurrencyTest {

	@Autowired
	private CouponIssueService issueService;

	@Autowired
	private CouponEventRepository eventRepository;

	@Autowired
	private CouponIssueRepository issueRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RedisIssueService redisIssueService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	@DisplayName("쿠폰 발급 동시성 테스트")
	void issueCouponConcurrently() throws Exception {
		int totalQuantity = 10;
		int requestCount = 30;
		CouponEvent event = saveIssuableEvent(totalQuantity);
		List<User> users = saveUsers(requestCount);
		initRedisIssueState(event);
		ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
		CountDownLatch ready = new CountDownLatch(requestCount);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger soldOutCount = new AtomicInteger();
		List<Future<?>> futures = new ArrayList<>();

		for (User user : users) {
			futures.add(executorService.submit(() -> {
				ready.countDown();
				start.await();

				try {
					issueService.issueCoupon(event.getId(), user.getId());
					successCount.incrementAndGet();
				} catch (BusinessException exception) {
					if (exception.getErrorCode() == CouponErrorCode.COUPON_SOLD_OUT) {
						soldOutCount.incrementAndGet();
						return null;
					}
					throw exception;
				}

				return null;
			}));
		}

		assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
		start.countDown();

		for (Future<?> future : futures) {
			future.get(10, TimeUnit.SECONDS);
		}

		executorService.shutdown();

		assertThat(successCount.get()).isEqualTo(totalQuantity);
		assertThat(soldOutCount.get()).isEqualTo(requestCount - totalQuantity);
		assertThat(issueRepository.countByCouponEventId(event.getId())).isEqualTo(totalQuantity);
		assertThat(eventRemainingQuantity(event)).isZero();
		assertThat(redisStock(event)).isEqualTo("0");
		assertThat(redisTemplate.opsForSet().size(CouponRedisKey.issuedUsers(event.getId())))
				  .isEqualTo(totalQuantity);
	}

	private User saveUser() {
		return userRepository.save(User.builder()
				  .name("test user " + UUID.randomUUID())
				  .build());
	}

	private List<User> saveUsers(int count) {
		List<User> users = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			users.add(saveUser());
		}

		return users;
	}

	private CouponEvent saveIssuableEvent(int totalQuantity) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime validIssueStartAt = now.plusDays(1);
		LocalDateTime validIssueEndAt = validIssueStartAt.plusDays(1);

		CouponEvent event = CouponEvent.create(
				  "test coupon " + UUID.randomUUID(),
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  1000L,
				  null,
				  totalQuantity,
				  validIssueStartAt,
				  validIssueEndAt
		);

		ReflectionTestUtils.setField(event, "issueStartAt", now.minusMinutes(1));
		ReflectionTestUtils.setField(event, "issueEndAt", now.plusDays(1));

		return eventRepository.saveAndFlush(event);
	}

	private void initRedisIssueState(CouponEvent event) {
		redisIssueService.initEventIssueState(
				  event.getId(),
				  event.getRemainingQuantity(),
				  event.getIssueStartAt(),
				  event.getIssueEndAt()
		);
	}

	private int eventRemainingQuantity(CouponEvent event) {
		return eventRepository.findById(event.getId())
				  .orElseThrow()
				  .getRemainingQuantity();
	}

	private String redisStock(CouponEvent event) {
		return redisTemplate.opsForValue().get(CouponRedisKey.stock(event.getId()));
	}
}
