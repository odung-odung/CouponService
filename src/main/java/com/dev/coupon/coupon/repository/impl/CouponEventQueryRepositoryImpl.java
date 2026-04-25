package com.dev.coupon.coupon.repository.impl;

import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import com.dev.coupon.coupon.domain.QCouponEvent;
import com.dev.coupon.coupon.dto.CouponEventResponse;
import com.dev.coupon.coupon.dto.condition.CouponEventCondition;
import com.dev.coupon.coupon.repository.CouponEventQueryRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class CouponEventQueryRepositoryImpl implements CouponEventQueryRepository {

	private final QCouponEvent couponEvent = QCouponEvent.couponEvent;
	private final JPAQueryFactory queryFactory;

	@Override
	public List<CouponEventResponse> search(CouponEventCondition condition) {
		return queryFactory
				  .select(Projections.constructor(
							 CouponEventResponse.class,
							 couponEvent.id,
							 couponEvent.name,
							 couponEvent.status,
							 couponEvent.discountType,
							 couponEvent.discountValue,
							 couponEvent.maxDiscountAmount,
							 couponEvent.totalQuantity,
							 couponEvent.issueStartAt,
							 couponEvent.issueEndAt
				  ))
				  .from(couponEvent)
				  .where(
							 keywordContains(condition.getKeyword()),
							 discountTypeEq(condition.getDiscountType()),
							 eventStatusEq(condition.getEventStatus()),
							 issueStartAtFrom(condition.getSearchStartAt()),
							 issueEndAtTo(condition.getSearchEndAt())
				  )
				  .fetch();
	}

	private BooleanExpression keywordContains(String keyword) {
		return hasText(keyword) ? couponEvent.name.contains(keyword) : null;
	}

	private BooleanExpression discountTypeEq(DiscountType discountType) {
		return discountType == null ? null : couponEvent.discountType.eq(discountType);
	}

	private BooleanExpression eventStatusEq(EventStatus eventStatus) {
		return eventStatus == null ? null : couponEvent.status.eq(eventStatus);
	}

	private BooleanExpression issueStartAtFrom(LocalDate from) {
		return from == null ? null : couponEvent.issueEndAt.goe(from.atStartOfDay());
	}

	private BooleanExpression issueEndAtTo(LocalDate to) {
		return to == null ? null : couponEvent.issueStartAt.lt(to.plusDays(1).atStartOfDay());
	}
}










