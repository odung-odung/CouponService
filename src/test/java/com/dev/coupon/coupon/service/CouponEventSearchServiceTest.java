package com.dev.coupon.coupon.service;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import com.dev.coupon.coupon.dto.CouponEventResponse;
import com.dev.coupon.coupon.dto.condition.CouponEventCondition;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Rollback(value = false)
@ActiveProfiles("test")
class CouponEventSearchServiceTest {

	@Autowired
	private CouponEventService couponEventService;

	@Autowired
	private CouponEventRepository eventRepository;

	@Test
	@DisplayName("검색 시작일이 종료일보다 뒤면 예외가 발생")
	void searchInvalidPeriod() {
		LocalDate searchStartAt = LocalDate.now().plusDays(2);
		LocalDate searchEndAt = LocalDate.now().plusDays(1);

		assertBusinessException(
				  () -> couponEventService.search(new CouponEventCondition(
							 null,
							 null,
							 null,
							 searchStartAt,
							 searchEndAt
				  )),
				  CouponErrorCode.INVALID_COUPON_EVENT_SEARCH_CONDITION
		);
	}

	@Test
	@DisplayName("이벤트 상태로 검색")
	void searchByEventStatus() {
		String keyword = "status-" + UUID.randomUUID();
		String openEventName = keyword + "-open";
		String closedEventName = keyword + "-closed";

		saveEvent(openEventName, EventStatus.OPEN, DiscountType.FIXED_AMOUNT);
		saveEvent(closedEventName, EventStatus.CLOSED, DiscountType.FIXED_AMOUNT);

		List<CouponEventResponse> responses = couponEventService.search(new CouponEventCondition(
				  EventStatus.OPEN,
				  null,
				  keyword,
				  null,
				  null
		));

		assertThat(responses)
				  .extracting(CouponEventResponse::getName)
				  .containsExactly(openEventName);
	}

	@Test
	@DisplayName("할인 타입으로 검색")
	void searchByDiscountType() {
		String keyword = "discount-" + UUID.randomUUID();
		String fixedAmountEventName = keyword + "-fixed";
		String percentageEventName = keyword + "-percentage";

		saveEvent(fixedAmountEventName, EventStatus.OPEN, DiscountType.FIXED_AMOUNT);
		saveEvent(percentageEventName, EventStatus.OPEN, DiscountType.PERCENTAGE);

		List<CouponEventResponse> responses = couponEventService.search(new CouponEventCondition(
				  null,
				  DiscountType.PERCENTAGE,
				  keyword,
				  null,
				  null
		));

		assertThat(responses)
				  .extracting(CouponEventResponse::getName)
				  .containsExactly(percentageEventName);
	}

	@Test
	@DisplayName("이벤트 이름 키워드로 검색")
	void searchByKeyword() {
		String keyword = "keyword-" + UUID.randomUUID();
		String matchedEventName = "matched-" + keyword;
		String unmatchedEventName = "unmatched-" + UUID.randomUUID();

		saveEvent(matchedEventName, EventStatus.OPEN, DiscountType.FIXED_AMOUNT);
		saveEvent(unmatchedEventName, EventStatus.OPEN, DiscountType.FIXED_AMOUNT);

		List<CouponEventResponse> responses = couponEventService.search(new CouponEventCondition(
				  null,
				  null,
				  keyword,
				  null,
				  null
		));

		assertThat(responses)
				  .extracting(CouponEventResponse::getName)
				  .containsExactly(matchedEventName);
	}

	@Test
	@DisplayName("검색 기간과 발급 기간이 겹치는 이벤트만 검색")
	void searchByOverlappedIssuePeriod() {
		String keyword = "period-" + UUID.randomUUID();
		LocalDate searchStartAt = LocalDate.now().plusDays(10);
		LocalDate searchEndAt = searchStartAt.plusDays(2);

		String overlappedEventName = keyword + "-overlapped";
		String beforeEventName = keyword + "-before";
		String afterEventName = keyword + "-after";

		saveEvent(
				  overlappedEventName,
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  LocalDateTime.of(searchStartAt, LocalTime.NOON),
				  LocalDateTime.of(searchEndAt, LocalTime.NOON)
		);
		saveEvent(
				  beforeEventName,
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  LocalDateTime.of(searchStartAt.minusDays(5), LocalTime.NOON),
				  LocalDateTime.of(searchStartAt.minusDays(1), LocalTime.NOON)
		);
		saveEvent(
				  afterEventName,
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  LocalDateTime.of(searchEndAt.plusDays(1), LocalTime.NOON),
				  LocalDateTime.of(searchEndAt.plusDays(2), LocalTime.NOON)
		);

		List<CouponEventResponse> responses = couponEventService.search(new CouponEventCondition(
				  null,
				  null,
				  keyword,
				  searchStartAt,
				  searchEndAt
		));

		assertThat(responses)
				  .extracting(CouponEventResponse::getName)
				  .containsExactly(overlappedEventName);
	}

	private CouponEvent saveEvent(String name, EventStatus status, DiscountType discountType) {
		LocalDateTime issueStartAt = LocalDateTime.now().plusDays(1);
		LocalDateTime issueEndAt = issueStartAt.plusDays(1);

		return saveEvent(name, status, discountType, issueStartAt, issueEndAt);
	}

	private CouponEvent saveEvent(
			  String name,
			  EventStatus status,
			  DiscountType discountType,
			  LocalDateTime issueStartAt,
			  LocalDateTime issueEndAt
	) {
		Long discountValue = discountType == DiscountType.PERCENTAGE ? 10L : 1000L;
		Long maxDiscountAmount = discountType == DiscountType.PERCENTAGE ? 5000L : null;

		CouponEvent event = CouponEvent.create(
				  name,
				  status,
				  discountType,
				  discountValue,
				  maxDiscountAmount,
				  100,
				  issueStartAt,
				  issueEndAt
		);

		return eventRepository.save(event);
	}

	private void assertBusinessException(Runnable runnable, CouponErrorCode errorCode) {
		assertThatThrownBy(runnable::run)
				  .isInstanceOf(BusinessException.class)
				  .satisfies(exception -> {
					  BusinessException businessException = (BusinessException) exception;
					  assertThat(businessException.getErrorCode()).isEqualTo(errorCode);
				  });
	}
}
