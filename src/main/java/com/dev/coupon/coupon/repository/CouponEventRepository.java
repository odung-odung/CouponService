package com.dev.coupon.coupon.repository;

import com.dev.coupon.coupon.domain.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponEventRepository extends JpaRepository<CouponEvent, Long>, CouponEventQueryRepository {
}
