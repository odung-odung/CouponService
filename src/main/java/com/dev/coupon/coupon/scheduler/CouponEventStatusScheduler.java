package com.dev.coupon.coupon.scheduler;

import com.dev.coupon.coupon.service.CouponEventStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponEventStatusScheduler {

	private final CouponEventStatusService eventStatusService;

	@Scheduled(fixedDelay = 60000)
	public void closeExpiredEvents() {
		int closedCount = eventStatusService.closeExpiredEvents();

		if (closedCount > 0) {
			log.info("[COUPON_EVENT_CLOSED] count = {}", closedCount);
		}
	}
}
