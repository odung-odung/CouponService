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
	@PositiveOrZero
	private Long discountValue;

	private long maxDiscountAmount;

	@NotNull
	@Positive
	private int totalQuantity;

	@NotNull
	@FutureOrPresent
	private LocalDateTime issueStartAt;

	@NotNull
	private LocalDateTime issueEndAt;
}
