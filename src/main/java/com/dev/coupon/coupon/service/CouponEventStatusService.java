package com.dev.coupon.coupon.service;

import com.dev.coupon.coupon.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponEventStatusService {

	private final CouponEventRepository eventRepository;
	private final Clock clock;

	@Transactional
	public int closeExpiredEvents() {
		return eventRepository.closeExpiredEvents(LocalDateTime.now(clock));
	}
}
