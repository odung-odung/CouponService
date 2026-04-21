package com.dev.coupon.coupon.dto;

import com.dev.coupon.common.BaseEntity;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponEventCreateRequest extends BaseEntity {

	@NotBlank
	private String name;

	@NotNull
	private EventStatus status;

	@NotNull
	private DiscountType discountType;

	@NotNull
	@Positive
	private Long discountValue;

	// @Positive를 걸지 않는다.
	private Long maxDiscountAmount;

	@NotNull
	@Positive
	private int totalQuantity;

	@NotNull
	private LocalDateTime issueStartAt;

	@NotNull
	private LocalDateTime issueEndAt;
}
