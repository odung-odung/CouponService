package com.dev.coupon.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handlerBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity.status(errorCode.getHttpStatus())
				  .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
	}
}
