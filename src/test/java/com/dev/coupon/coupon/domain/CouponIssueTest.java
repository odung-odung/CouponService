package com.dev.coupon.coupon.domain;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import com.dev.coupon.coupon.exception.ExpiredCouponException;
import com.dev.coupon.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponIssueTest {

	@Test
	@DisplayName("쿠폰을 발급하면 발급 상태로 생성되고 사용 시간은 없다")
	void issue() {
		LocalDateTime issuedAt = LocalDateTime.now();

		CouponIssue couponIssue = CouponIssue.issue(
				  createCouponEvent(issuedAt),
				  createUser(),
				  issuedAt
		);

		assertThat(couponIssue.getStatus()).isEqualTo(IssueStatus.ISSUED);
		assertThat(couponIssue.getIssuedAt()).isEqualTo(issuedAt);
		assertThat(couponIssue.getUsedAt()).isNull();
	}

	@Test
	@DisplayName("발급된 쿠폰을 사용하면 사용 상태와 사용 시간이 기록된다")
	void use() {
		LocalDateTime issuedAt = LocalDateTime.now();
		CouponEvent couponEvent = createCouponEvent(issuedAt);
		LocalDateTime usedAt = couponEvent.getIssueEndAt().minusSeconds(1);
		CouponIssue couponIssue = CouponIssue.issue(couponEvent, createUser(), issuedAt);

		couponIssue.use(usedAt);

		assertThat(couponIssue.getStatus()).isEqualTo(IssueStatus.USED);
		assertThat(couponIssue.getUsedAt()).isEqualTo(usedAt);
	}

	@Test
	@DisplayName("이미 사용한 쿠폰은 다시 사용할 수 없다")
	void cannotUseAlreadyUsedCoupon() {
		LocalDateTime issuedAt = LocalDateTime.now();
		CouponEvent couponEvent = createCouponEvent(issuedAt);
		LocalDateTime usedAt = couponEvent.getIssueEndAt().minusSeconds(1);
		CouponIssue couponIssue = CouponIssue.issue(couponEvent, createUser(), issuedAt);
		couponIssue.use(usedAt);

		assertBusinessException(
				  () -> couponIssue.use(usedAt.plusSeconds(1)),
				  CouponErrorCode.COUPON_ALREADY_USED
		);
	}

	@Test
	@DisplayName("발급 종료 시각이 지난 쿠폰은 만료 상태가 되고 사용할 수 없다")
	void expireWhenUsedAfterIssueEndAt() {
		LocalDateTime issuedAt = LocalDateTime.now();
		CouponEvent couponEvent = createCouponEvent(issuedAt);
		CouponIssue couponIssue = CouponIssue.issue(couponEvent, createUser(), issuedAt);

		assertThatThrownBy(() -> couponIssue.use(couponEvent.getIssueEndAt()))
				  .isInstanceOf(ExpiredCouponException.class)
				  .satisfies(exception -> {
					  BusinessException businessException = (BusinessException) exception;
					  assertThat(businessException.getErrorCode()).isEqualTo(CouponErrorCode.COUPON_EXPIRED);
				  });
		assertThat(couponIssue.getStatus()).isEqualTo(IssueStatus.EXPIRED);
		assertThat(couponIssue.getUsedAt()).isNull();
	}

	private CouponEvent createCouponEvent(LocalDateTime issuedAt) {
		LocalDateTime issueStartAt = issuedAt.plusHours(1);

		return CouponEvent.create(
				  "test coupon",
				  EventStatus.OPEN,
				  DiscountType.FIXED_AMOUNT,
				  1000L,
				  null,
				  100,
				  issueStartAt,
				  issueStartAt.plusDays(1),
				  issuedAt
		);
	}

	private User createUser() {
		return User.builder()
				  .name("test user")
				  .build();
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
