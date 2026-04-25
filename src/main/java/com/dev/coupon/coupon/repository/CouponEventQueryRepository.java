package com.dev.coupon.coupon.repository;

import com.dev.coupon.coupon.dto.CouponEventResponse;
import com.dev.coupon.coupon.dto.condition.CouponEventCondition;

import java.util.List;

public interface CouponEventQueryRepository {
	List<CouponEventResponse> search(CouponEventCondition condition);
}
