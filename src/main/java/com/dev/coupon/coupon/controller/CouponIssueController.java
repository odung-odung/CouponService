package com.dev.coupon.coupon.controller;

import com.dev.coupon.common.ApiResponse;
import com.dev.coupon.coupon.dto.CouponIssueRequest;
import com.dev.coupon.coupon.service.CouponIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {

	private final CouponIssueService issueService;

	@PostMapping("/api/coupon-events/{couponEventId}/issues")
	public ResponseEntity<ApiResponse<Long>> issue(
			  @PathVariable Long couponEventId,
			  @Valid @RequestBody CouponIssueRequest request
	) {
		Long couponIssueId = issueService.issueCoupon(couponEventId, request.getUserId());
		return ResponseEntity
				  .status(201)
				  .body(ApiResponse.success(couponIssueId));
	}
}
