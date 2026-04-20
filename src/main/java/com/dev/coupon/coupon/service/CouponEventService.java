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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponEventService {

	private final CouponEventRepository repository;

	@Transactional
	public CouponEventResponse create(CouponEventCreateRequest request) {
		if (request.getDiscountType() == DiscountType.FIXED_AMOUNT) {
			throw new BusinessException(CouponErrorCode.FIXED_AMOUNT_MAX_DISCOUNT_NOT_ALLOWED);
		}

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
