package com.dev.coupon.coupon.dto;

import com.dev.coupon.coupon.domain.DiscountType;
import com.dev.coupon.coupon.domain.EventStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CouponEventCreateRequestValidationTest {

	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void closeValidatorFactory() {
		validatorFactory.close();
	}

	@Test
	@DisplayName("에러없는 정상 요청")
	void validRequest() {
		CouponEventCreateRequest request = createValidRequest();

		Set<ConstraintViolation<CouponEventCreateRequest>> violations = validator.validate(request);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("쿠폰 이벤트 이름은 공백일 수 없다")
	void nameNotBlank() {
		assertViolation("name", " ");
	}

	@Test
	@DisplayName("이벤트 상태는 필수")
	void statusNotNull() {
		assertViolation("status", null);
	}

	@Test
	@DisplayName("할인 타입은 필수")
	void discountTypeNotNull() {
		assertViolation("discountType", null);
	}

	@Test
	@DisplayName("할인 값은 필수")
	void discountValueNotNull() {
		assertViolation("discountValue", null);
	}

	@Test
	@DisplayName("할인 값은 양수여야 한다")
	void discountValuePositive() {
		assertViolation("discountValue", 0L);
	}

	@Test
	@DisplayName("전체 수량은 양수여야 한다")
	void totalQuantityPositive() {
		assertViolation("totalQuantity", 0);
	}

	@Test
	@DisplayName("발급 시작 일시는 필수")
	void issueStartAtNotNull() {
		assertViolation("issueStartAt", null);
	}

	@Test
	@DisplayName("발급 종료 일시는 필수")
	void issueEndAtNotNull() {
		assertViolation("issueEndAt", null);
	}

	private void assertViolation(String fieldName, Object value) {
		CouponEventCreateRequest request = createValidRequest();
		ReflectionTestUtils.setField(request, fieldName, value);

		Set<ConstraintViolation<CouponEventCreateRequest>> violations = validator.validate(request);

		assertThat(violations)
				  .extracting(violation -> violation.getPropertyPath().toString())
				  .contains(fieldName);
	}

	private CouponEventCreateRequest createValidRequest() {
		CouponEventCreateRequest request = new CouponEventCreateRequest();

		ReflectionTestUtils.setField(request, "name", "test coupon");
		ReflectionTestUtils.setField(request, "status", EventStatus.OPEN);
		ReflectionTestUtils.setField(request, "discountType", DiscountType.FIXED_AMOUNT);
		ReflectionTestUtils.setField(request, "discountValue", 1000L);
		ReflectionTestUtils.setField(request, "maxDiscountAmount", null);
		ReflectionTestUtils.setField(request, "totalQuantity", 100);
		ReflectionTestUtils.setField(request, "issueStartAt", LocalDateTime.now().plusDays(1));
		ReflectionTestUtils.setField(request, "issueEndAt", LocalDateTime.now().plusDays(2));

		return request;
	}
}
