package com.dev.coupon.user.repository;

import com.dev.coupon.user.dto.MyCouponListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserCouponQueryRepository {
	Page<MyCouponListResponse> getMyCoupons(Long userId, Pageable pageable);
}
