package com.dev.coupon.coupon.exception;

import com.dev.coupon.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CouponErrorCode implements ErrorCode {

	FIXED_AMOUNT_MAX_DISCOUNT_NOT_ALLOWED(
			  HttpStatus.BAD_REQUEST,
			  "FIXED_AMOUNT_MAX_DISCOUNT_NOT_ALLOWED",
			  "고정 금액 할인 쿠폰은 maxDiscountAmount를 설정 할 수 없습니다."
	);

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	CouponErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
