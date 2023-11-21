package com.ll.netmong.domain.product.controller;

import com.ll.netmong.common.RsData;
import com.ll.netmong.domain.product.dto.request.CreateRequest;
import com.ll.netmong.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public RsData createProduct(@RequestBody CreateRequest createRequest) {
        productService.createProduct(createRequest);
        return RsData.of("S-1", "상품이 등록되었습니다.");
    }
}
