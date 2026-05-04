package com.dev.coupon.coupon.service;

import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
@ActiveProfiles("test")
class CouponEventStatusServiceTest {

	@Autowired
	private CouponEventStatusService eventStatusService;

	@Autowired
	private CouponEventRepository eventRepository;

	@Test
	@DisplayName("Status가 OPEN인데 기간이 만료된 이벤트를 CLOSED")
	void closeExpiredOpenEvents() {
		eventRepository.closeExpiredEvents(LocalDateTime.now());

		LocalDateTime now = LocalDateTime.now();
		CouponEvent expiredOpenEvent = saveEventWithPeriod(
				  EventStatus.OPEN,
				  now.minusDays(2),
				  now.minusDays(1)
		);
		CouponEvent activeOpenEvent = saveEventWithPeriod(
				  EventStatus.OPEN,
				  now.plusDays(1),
				  now.plusDays(2)
		);
		CouponEvent expiredClosedEvent = saveEventWithPeriod(
				  EventStatus.CLOSED,
				  now.minusDays(2),
				  now.minusDays(1)
		);

		int closedCount = eventStatusService.closeExpiredEvents();

		assertThat(closedCount).isEqualTo(1);
		assertThat(eventRepository.findById(expiredOpenEvent.getId()).orElseThrow().getStatus())
				  .isEqualTo(EventStatus.CLOSED);
		assertThat(eventRepository.findById(activeOpenEvent.getId()).orElseThrow().getStatus())
				  .isEqualTo(EventStatus.OPEN);
		assertThat(eventRepository.findById(expiredClosedEvent.getId()).orElseThrow().getStatus())
				  .isEqualTo(EventStatus.CLOSED);
	}

	private CouponEvent saveEventWithPeriod(
			  EventStatus status,
			  LocalDateTime issueStartAt,
			  LocalDateTime issueEndAt
	) {
		LocalDateTime validIssueStartAt = LocalDateTime.now().plusDays(1);
		LocalDateTime validIssueEndAt = validIssueStartAt.plusDays(1);

		CouponEvent event = CouponEvent.create(
				  "status-" + UUID.randomUUID(),
				  status,
				  DiscountType.FIXED_AMOUNT,
				  1000L,
				  null,
				  100,
				  validIssueStartAt,
				  validIssueEndAt
		);

		ReflectionTestUtils.setField(event, "issueStartAt", issueStartAt);
		ReflectionTestUtils.setField(event, "issueEndAt", issueEndAt);

		return eventRepository.saveAndFlush(event);
	}
}
