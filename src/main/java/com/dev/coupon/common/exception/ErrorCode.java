package com.dev.coupon.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	public HttpStatus getHttpStatus();
	public String getCode();
	public String getMessage();
}
