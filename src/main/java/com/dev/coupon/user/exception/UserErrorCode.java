package com.dev.coupon.user.exception;

import com.dev.coupon.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {
	USER_NOT_FOUND(HttpStatus.BAD_REQUEST,"USER_NOT_FOUND", "존재하지 않는 유저입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	UserErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
