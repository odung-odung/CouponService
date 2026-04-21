package com.dev.coupon.coupon.service;

import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.coupon.domain.CouponEvent;
import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.dto.CouponEventCreateRequest;
import com.dev.coupon.coupon.dto.CouponEventResponse;
import com.dev.coupon.coupon.exception.CouponErrorCode;
import com.dev.coupon.coupon.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponEventService {

	private final CouponEventRepository repository;

	@Transactional
	public CouponEventResponse create(CouponEventCreateRequest request) {

		validateCreatePolicy(request);

		CouponEvent event = repository.save(CouponEvent.builder()
				  .name(request.getName())
				  .status(request.getStatus())
				  .discountType(request.getDiscountType())
				  .discountValue(request.getDiscountValue())
				  .maxDiscountAmount(request.getMaxDiscountAmount())
				  .totalQuantity(request.getTotalQuantity())
				  .issueStartAt(request.getIssueStartAt())
				  .issueEndAt(request.getIssueEndAt())
				  .build());
		return new CouponEventResponse(
				  event.getId(),
				  event.getName(),
				  event.getStatus(),
				  event.getDiscountType(),
				  event.getDiscountValue(),
				  event.getMaxDiscountAmount(),
				  event.getTotalQuantity(),
				  event.getIssueStartAt(),
				  event.getIssueEndAt()
		);
	}

	private void validateCreatePolicy(CouponEventCreateRequest request) {
		// 고정 금액 할인은 최대 할인 금액 필요 없음 discountValue로만 설정
		if (request.getDiscountType() == DiscountType.FIXED_AMOUNT) {
			if(request.getMaxDiscountAmount() != null) {
				throw new BusinessException(CouponErrorCode.FIXED_AMOUNT_MAX_DISCOUNT_NOT_ALLOWED);
			}
		}

		// % 할인은 최대 할인 금액이 필수고 0 보다 커야 함
		if (request.getDiscountType() == DiscountType.PERCENTAGE) {
			Long maxDiscountAmount = request.getMaxDiscountAmount();

			if (maxDiscountAmount == null || maxDiscountAmount <= 0) {
				throw new BusinessException(CouponErrorCode.PERCENTAGE_MAX_DISCOUNT_REQUIRED);
			}
		}

		// % 할인은 discountValue가 100보다 작아야 함
		if (request.getDiscountType() == DiscountType.PERCENTAGE && request.getDiscountValue() > 100) {
			throw new BusinessException(CouponErrorCode.INVALID_PERCENTAGE_DISCOUNT_VALUE);
		}

		// 쿠폰 만료 날짜는 시작 날짜 이후여야 함
		if (!request.getIssueEndAt().isAfter(request.getIssueStartAt())) {
			throw new BusinessException(CouponErrorCode.INVALID_ISSUE_START_AT);
		}

		// 쿠폰 시작 날짜는 현재 시간보다 이전일 수 없다
		if (request.getIssueStartAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException(CouponErrorCode.INVALID_ISSUE_END_AT);
		}
	}

	public List<CouponEventResponse> getCouponEvents() {
		return repository.findAll().stream().map(event ->
				  new CouponEventResponse(
							 event.getId(),
							 event.getName(),
							 event.getStatus(),
							 event.getDiscountType(),
							 event.getDiscountValue(),
							 event.getMaxDiscountAmount(),
							 event.getTotalQuantity(),
							 event.getIssueStartAt(),
							 event.getIssueEndAt()
				  )).toList();
	}
}
