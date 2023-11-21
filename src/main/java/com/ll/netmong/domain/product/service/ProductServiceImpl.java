package com.ll.netmong.domain.product.service;

import com.ll.netmong.domain.product.dto.request.CreateRequest;
import com.ll.netmong.domain.product.entity.Product;
import com.ll.netmong.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public void createProduct(CreateRequest createRequest) {
        productRepository.save(Product.builder()
                .productName(createRequest.getProductName())
                .price(createRequest.getPrice())
                .content(createRequest.getContent()).build());
    }
}
