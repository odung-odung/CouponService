package com.dev.coupon.coupon.domain;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponEventTest {

	@Test
	@DisplayName("쿠폰 이벤트 생성 시 남은 수량은 전체 수량으로 초기화되고 재동기화 상태는 false")
	void create() {
		LocalDateTime now = LocalDateTime.now();

		CouponEvent couponEvent = CouponEvent.create(
				  "test coupon",
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  1000L,
				  null,
				  100,
				  now.plusMinutes(1),
				  now.plusDays(1)
		);

		assertThat(couponEvent.getRemainingQuantity()).isEqualTo(100);
		assertThat(couponEvent.isStockResyncPending()).isFalse();
	}

	@Test
	@DisplayName("발급 시작 시간이 현재 시간 이전이면 생성할 수 없다")
	void invalidIssueStartAt() {
		LocalDateTime now = LocalDateTime.now();

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.FIXED_AMOUNT,
							 1000L,
							 null,
							 100,
							 now.minusSeconds(1),
							 now.plusDays(1)
				  ),
				  CouponErrorCode.INVALID_ISSUE_START_AT
		);
	}

	@Test
	@DisplayName("발급 종료 시간이 시작 시간 이전이거나 같으면 생성할 수 없다")
	void invalidIssueEndAt() {
		LocalDateTime issueStartAt = LocalDateTime.now().plusDays(1);

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.FIXED_AMOUNT,
							 1000L,
							 null,
							 100,
							 issueStartAt,
							 issueStartAt
				  ),
				  CouponErrorCode.INVALID_ISSUE_END_AT
		);
	}

	@Test
	@DisplayName("전체 수량이 0 이하이면 생성할 수 없다")
	void invalidTotalQuantity() {
		LocalDateTime now = LocalDateTime.now();

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.FIXED_AMOUNT,
							 1000L,
							 null,
							 0,
							 now.plusMinutes(1),
							 now.plusDays(1)
				  ),
				  CouponErrorCode.INVALID_COUPON_EVENT_TOTAL_QUANTITY
		);
	}

	@Test
	@DisplayName("고정 금액 할인은 최대 할인 금액을 설정할 수 없다")
	void fixedAmountMaxDiscountNotAllowed() {
		LocalDateTime now = LocalDateTime.now();

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.FIXED_AMOUNT,
							 1000L,
							 5000L,
							 100,
							 now.plusMinutes(1),
							 now.plusDays(1)
				  ),
				  CouponErrorCode.FIXED_AMOUNT_MAX_DISCOUNT_NOT_ALLOWED
		);
	}

	@Test
	@DisplayName("정률 할인은 최대 할인 금액이 필요하다")
	void percentageMaxDiscountRequired() {
		LocalDateTime now = LocalDateTime.now();

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.PERCENTAGE,
							 10L,
							 null,
							 100,
							 now.plusMinutes(1),
							 now.plusDays(1)
				  ),
				  CouponErrorCode.PERCENTAGE_MAX_DISCOUNT_REQUIRED
		);
	}

	@Test
	@DisplayName("정률 할인은 최대 할인 금액이 0보다 커야 한다")
	void percentageMaxDiscountMustBePositive() {
		LocalDateTime now = LocalDateTime.now();

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.PERCENTAGE,
							 10L,
							 0L,
							 100,
							 now.plusMinutes(1),
							 now.plusDays(1)
				  ),
				  CouponErrorCode.PERCENTAGE_MAX_DISCOUNT_REQUIRED
		);
	}

	@Test
	@DisplayName("정률 할인율이 100을 초과하면 생성할 수 없다")
	void invalidPercentageDiscountValue() {
		LocalDateTime now = LocalDateTime.now();

		assertBusinessException(
				  () -> CouponEvent.create(
							 "test coupon",
							 EventStatus.OPEN,
							 DiscountType.PERCENTAGE,
							 101L,
							 5000L,
							 100,
							 now.plusMinutes(1),
							 now.plusDays(1)
				  ),
				  CouponErrorCode.INVALID_PERCENTAGE_DISCOUNT_VALUE
		);
	}

	@Test
	@DisplayName("고정 금액 할인은 설정된 금액만큼 할인된다")
	void calculateFixedAmountDiscount() {
		CouponEvent couponEvent = createFixedAmountEvent(1000L);

		Long discountAmount = couponEvent.calculateDiscountAmount(10000L);

		assertThat(discountAmount).isEqualTo(1000L);
	}

	@Test
	@DisplayName("고정 금액 할인이 상품 가격보다 크면 상품 가격까지만 할인된다")
	void calculateFixedAmountDiscountCannotExceedProductPrice() {
		CouponEvent couponEvent = createFixedAmountEvent(10000L);

		Long discountAmount = couponEvent.calculateDiscountAmount(5000L);

		assertThat(discountAmount).isEqualTo(5000L);
	}

	@Test
	@DisplayName("정률 할인은 상품 가격에 할인율을 적용해서 계산된다")
	void calculatePercentageDiscount() {
		CouponEvent couponEvent = createPercentageEvent(10L, 5000L);

		Long discountAmount = couponEvent.calculateDiscountAmount(10000L);

		assertThat(discountAmount).isEqualTo(1000L);
	}

	@Test
	@DisplayName("정률 할인은 최대 할인 금액을 넘을 수 없다")
	void calculatePercentageDiscountCannotExceedMaxDiscountAmount() {
		CouponEvent couponEvent = createPercentageEvent(50L, 3000L);

		Long discountAmount = couponEvent.calculateDiscountAmount(10000L);

		assertThat(discountAmount).isEqualTo(3000L);
	}

	private void assertBusinessException(Runnable runnable, CouponErrorCode errorCode) {
		assertThatThrownBy(runnable::run)
				  .isInstanceOf(BusinessException.class)
				  .satisfies(exception -> {
					  BusinessException businessException = (BusinessException) exception;
					  assertThat(businessException.getErrorCode()).isEqualTo(errorCode);
				  });
	}

	private CouponEvent createFixedAmountEvent(Long discountValue) {
		LocalDateTime now = LocalDateTime.now();

		return CouponEvent.create(
				  "test coupon",
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  discountValue,
				  null,
				  100,
				  now.plusMinutes(1),
				  now.plusDays(1)
		);
	}

	private CouponEvent createPercentageEvent(Long discountValue, Long maxDiscountAmount) {
		LocalDateTime now = LocalDateTime.now();

		return CouponEvent.create(
				  "test coupon",
				  EventStatus.OPEN,
				  DiscountType.PERCENTAGE,
				  discountValue,
				  maxDiscountAmount,
				  100,
				  now.plusMinutes(1),
				  now.plusDays(1)
		);
	}
}
