package com.dev.coupon.coupon.dto.condition;

import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CouponEventCondition {

	private final EventStatus eventStatus;
	private final DiscountType discountType;
	private final String keyword;
	private final LocalDate searchStartAt;
	private final LocalDate searchEndAt;
}
