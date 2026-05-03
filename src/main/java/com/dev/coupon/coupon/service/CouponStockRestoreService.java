package com.dev.coupon.coupon.service;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.common.exception.SystemException;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import com.dev.coupon.coupon.exception.SystemErrorCode;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import com.dev.coupon.coupon.repository.CouponIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponStockRestoreService {

	private final CouponEventRepository eventRepository;
	private final CouponIssueRepository issueRepository;
	private final RedisRecoveryService redisRecoveryService;

	@Transactional
	public void restoreFromDatabase(Long eventId) {
		CouponEvent couponEvent = eventRepository.findById(eventId)
				  .orElseThrow(()-> new BusinessException(CouponErrorCode.COUPON_EVENT_NOT_FOUND)
		);

		int totalQuantity = couponEvent.getTotalQuantity();
		int issuedQuantity = issueRepository.countByCouponEventId(eventId);

		if (totalQuantity < issuedQuantity) {
			log.error(
					  "[COUPON_STOCK_INCONSISTENT] eventId={}, totalQuantity={}, issuedQuantity={}",
					  eventId,
					  totalQuantity,
					  issuedQuantity
			);
			throw new SystemException(SystemErrorCode.COUPON_STOCK_INCONSISTENT);
		}

		List<Long> userIds = issueRepository.findUserIdByCouponEventId(eventId);

		int remainingQuantity = totalQuantity - issuedQuantity;
		LocalDateTime issueStartAt = couponEvent.getIssueStartAt();
		LocalDateTime issueEndAt = couponEvent.getIssueEndAt();

		redisRecoveryService.restoreStock(
				  eventId,
				  userIds,
				  remainingQuantity,
				  issueStartAt,
				  issueEndAt
		);

		completeStockResync(eventId, remainingQuantity, issueStartAt, issueEndAt);
	}

	private void completeStockResync(Long eventId, int remainingQuantity, LocalDateTime issueStartAt, LocalDateTime issueEndAt) {
		int updated = eventRepository.completeStockResync(eventId, remainingQuantity, issueStartAt, issueEndAt);

		if (updated == 0) {
			log.error("[COUPON_STOCK_RESYNC_COMPLETE_FAILED] eventId={}, remainingQuantity={}", eventId, remainingQuantity);
			throw new SystemException(SystemErrorCode.COUPON_STOCK_RESYNC_COMPLETE_FAILED);
		}
	}
}
