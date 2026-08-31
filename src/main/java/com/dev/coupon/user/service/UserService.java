package com.dev.coupon.user.service;

import com.dev.coupon.common.PageResponse;
import com.dev.coupon.common.exception.BusinessException;
import com.dev.coupon.coupon.repository.CouponIssueQueryRepository;
import com.dev.coupon.user.dto.CouponPageRequest;
import com.dev.coupon.coupon.dto.UserCouponResponse;
import com.dev.coupon.user.exception.UserErrorCode;
import com.dev.coupon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final CouponIssueQueryRepository repository;


	@Transactional(readOnly = true)
	public PageResponse<UserCouponResponse> getUsableCouponsByUserId(Long userId, CouponPageRequest request) {
		if (!userRepository.existsById(userId)) {
			throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
		}

		PageRequest pageable = PageRequest.of(
			request.getPage() == null ? 0 : request.getPage() - 1,
			request.getSize() == null ? 10 : request.getSize(),
			Sort.by(Sort.Direction.DESC, "id")
		);

		Page<UserCouponResponse> page = repository.findUsableCouponsByUserId(userId, pageable);
		return PageResponse.from(page);
	}
}
