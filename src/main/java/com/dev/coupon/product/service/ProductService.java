package com.dev.coupon.product.service;

import com.dev.coupon.product.domain.Product;
import com.dev.coupon.product.dto.PageResponse;
import com.dev.coupon.product.dto.ProductCreateRequest;
import com.dev.coupon.product.dto.ProductResponse;
import com.dev.coupon.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository repository;

	@Transactional
	public ProductResponse create(ProductCreateRequest request) {
		Product product = repository.save(Product.builder()
				  .name(request.getName())
				  .price(request.getPrice())
				  .build());
		return ProductResponse.from(product);
	}

	@Transactional(readOnly = true)
	public PageResponse<ProductResponse> getProductPage(Pageable pageable) {
		Page<ProductResponse> page = repository.findAll(pageable)
				  .map(ProductResponse::from);

		return PageResponse.from(page);
	}
}
