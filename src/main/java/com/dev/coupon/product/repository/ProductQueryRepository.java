package com.dev.coupon.product.repository;

import com.dev.coupon.product.dto.ProductCondition;
import com.dev.coupon.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {
	Page<ProductResponse> search(ProductCondition condition, Pageable pageable);
}
