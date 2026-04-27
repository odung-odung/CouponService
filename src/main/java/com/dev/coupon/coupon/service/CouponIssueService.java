package com.dev.coupon.coupon.service;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.CouponIssue;
import com.dev.coupon.coupon.domain.IssueStatus;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import com.dev.coupon.coupon.repository.CouponIssueRepository;
import com.dev.coupon.user.domain.User;
import com.dev.coupon.user.exception.UserErrorCode;
import com.dev.coupon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

	private final CouponIssueRepository issueRepository;
	private final CouponEventRepository eventRepository;
	private final UserRepository userRepository;

	@Transactional
	public Long issueCoupon(Long couponEventId, Long userId) {
		User findUser = userRepository.findById(userId)
				  .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

		CouponEvent findEvent = eventRepository.findById(couponEventId)
				  .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_EVENT_NOT_FOUND));

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime issueStartAt = findEvent.getIssueStartAt();
		LocalDateTime issueEndAt = findEvent.getIssueEndAt();

		/*
		  발급 불가능 시간 검증
		 */
		if (issueStartAt.isAfter(now) || !issueEndAt.isAfter(now)) {
			throw new BusinessException(CouponErrorCode.COUPON_NOT_ISSUABLE);
		}

		CouponIssue couponIssue = issueRepository.save(new CouponIssue(
				  findEvent,
				  findUser,
				  IssueStatus.ISSUED,
				  now,
				  null
		));

		return couponIssue.getId();
	}
}
