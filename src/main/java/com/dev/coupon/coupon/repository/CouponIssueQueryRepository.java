package com.dev.coupon.coupon.repository;

import com.dev.coupon.coupon.dto.UserCouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponIssueQueryRepository {
	Page<UserCouponResponse> findUsableCouponsByUserId(Long userId, Pageable pageable);
}
