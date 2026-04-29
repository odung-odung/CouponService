package com.dev.coupon.coupon.exception;

import com.dev.coupon.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RedisErrorCode implements ErrorCode {
	REDIS_ISSUE_INVALID_RESULT(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_ISSUE_INVALID_RESULT" ,
			  "쿠폰 예약 처리 중 Redis 스크립트 결과가 유효하지 않습니다."),
	REDIS_ISSUE_EXECUTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_ISSUE_EXECUTION_FAILED",
			  "쿠폰 예약 처리 중 Redis 스크립트 실행에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	RedisErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
