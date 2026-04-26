package com.dev.coupon.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class CouponPageRequest {

	@Min(1)
	private Integer page;

	@Min(1)
	@Max(100)
	private Integer size;

	public Pageable toPageable() {
		int p = (page == null) ? 0 : page - 1;
		int s = (size == null) ? 10 : size;
		return PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
	}


}
