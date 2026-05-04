package com.dev.coupon.coupon.service;

import com.dev.coupon.common.util.CouponRedisKey;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.CouponIssue;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import com.dev.coupon.coupon.domain.IssueStatus;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CouponStockRecoveryServiceTest {

	@Autowired
	private CouponStockRecoveryService recoveryService;

	@Autowired
	private CouponEventRepository eventRepository;

	@Autowired
	private CouponIssueRepository issueRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	@DisplayName("재동기화 대기 이벤트 재고 복구")
	void resyncPendingEventStock() {
		CouponEvent event = savePendingEvent(10);
		List<User> issuedUsers = saveIssuedUsers(event, 3);
		initBrokenRedisState(event);

		recoveryService.resync(event.getId());

		CouponEvent recoveredEvent = eventRepository.findById(event.getId()).orElseThrow();

		assertThat(recoveredEvent.getRemainingQuantity()).isEqualTo(7);
		assertThat(recoveredEvent.isStockResyncPending()).isFalse();
		assertThat(redisTemplate.opsForValue().get(CouponRedisKey.stock(event.getId()))).isEqualTo("7");
		assertThat(redisTemplate.opsForSet().size(CouponRedisKey.issuedUsers(event.getId()))).isEqualTo(3);
		assertThat(redisTemplate.opsForValue().get(CouponRedisKey.issueStartAt(event.getId())))
				  .isEqualTo(String.valueOf(toEpochMillis(event.getIssueStartAt())));
		assertThat(redisTemplate.opsForValue().get(CouponRedisKey.issueEndAt(event.getId())))
				  .isEqualTo(String.valueOf(toEpochMillis(event.getIssueEndAt())));

		for (User user : issuedUsers) {
			assertThat(redisTemplate.opsForSet().isMember(
					  CouponRedisKey.issuedUsers(event.getId()),
					  user.getId().toString()
			)).isTrue();
		}
	}

	private CouponEvent savePendingEvent(int totalQuantity) {
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
		ReflectionTestUtils.setField(event, "stockResyncPending", true);

		return eventRepository.saveAndFlush(event);
	}

	private List<User> saveIssuedUsers(CouponEvent event, int count) {
		List<User> users = new ArrayList<>();

		for (int i = 0; i < count; i++) {
			User user = userRepository.save(User.builder()
					  .name("test user " + UUID.randomUUID())
					  .build());
			issueRepository.save(new CouponIssue(
					  event,
					  user,
					  IssueStatus.ISSUED,
					  LocalDateTime.now().minusMinutes(10),
					  null
			));
			users.add(user);
		}

		issueRepository.flush();
		return users;
	}

	private void initBrokenRedisState(CouponEvent event) {
		redisTemplate.opsForValue().set(CouponRedisKey.stock(event.getId()), "999");
		redisTemplate.opsForValue().set(CouponRedisKey.issueStartAt(event.getId()), "1");
		redisTemplate.opsForValue().set(CouponRedisKey.issueEndAt(event.getId()), "2");
		redisTemplate.opsForSet().add(CouponRedisKey.issuedUsers(event.getId()), "-1", "-2");
	}

	private long toEpochMillis(LocalDateTime dateTime) {
		return dateTime.atZone(ZoneId.of("Asia/Seoul"))
				  .toInstant()
				  .toEpochMilli();
	}
}
