package com.dev.coupon.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponPageRequest {

	@Min(1)
	private Integer page;

	@Min(1)
	@Max(100)
	private Integer size;

}
