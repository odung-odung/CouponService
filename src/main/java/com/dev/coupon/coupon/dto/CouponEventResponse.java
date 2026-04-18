package com.dev.coupon.coupon.dto;

import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponEventResponse {

	private final Long id;
	private final String name;
	private final EventStatus status;
	private final DiscountType discountType;
	private final Long discountValue;
	private final Long maxDiscountAmount;
	private final Integer totalQuantity;
	private final LocalDateTime issueStartAt;
	private final LocalDateTime issueEndAt;

	public CouponEventResponse(
		Long id,
		String name,
		EventStatus status,
		DiscountType discountType,
		Long discountValue,
		Long maxDiscountAmount,
		Integer totalQuantity,
		LocalDateTime issueStartAt,
		LocalDateTime issueEndAt
	) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.maxDiscountAmount = maxDiscountAmount;
		this.totalQuantity = totalQuantity;
		this.issueStartAt = issueStartAt;
		this.issueEndAt = issueEndAt;
	}


}
