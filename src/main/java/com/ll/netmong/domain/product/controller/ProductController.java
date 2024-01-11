package com.ll.netmong.domain.product.controller;

import com.ll.netmong.common.PageResponse;
import com.ll.netmong.common.ProductException;
import com.ll.netmong.common.RsData;
import com.ll.netmong.domain.product.dto.request.CreateRequest;
import com.ll.netmong.domain.product.dto.request.UpdateRequest;
import com.ll.netmong.domain.product.dto.response.ViewAllResponse;
import com.ll.netmong.domain.product.dto.response.ViewSingleResponse;
import com.ll.netmong.domain.product.service.ProductService;
import com.ll.netmong.domain.product.util.Category;
import com.ll.netmong.domain.product.util.ProductErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private static final String FIND_SUCCESS_PRODUCT = "상품 조회가 되었습니다.";
    private static final String POST_SUCCESS_PRODUCT = "상품이 등록 되었습니다.";
    private static final String MODIFY_SUCCESS_PRODUCT = "상품이 수정 되었습니다.";
    private static final String DELETE_SUCCESS_PRODUCT = "상품이 삭제 되었습니다.";
    private static final String PAGE_START_NUMBER = "1";
    private final ProductService productService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RsData findByAllProducts() {
        List<ViewAllResponse> viewAllProducts = productService.viewAllProducts();
        return RsData.of("S-1", FIND_SUCCESS_PRODUCT, viewAllProducts);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RsData findByProduct(@PathVariable(name = "id") Long productId) {
        ViewSingleResponse viewSingleProduct = productService.findByProduct(productId);
        return RsData.of("S-1", FIND_SUCCESS_PRODUCT, viewSingleProduct);
    }

    @GetMapping("/category/{category}")
    public RsData findByProductCategory(@PathVariable(name = "category") String category) {
        List<ViewAllResponse> viewAllByProductCategory = new ArrayList<>();
        try {
            viewAllByProductCategory.addAll(productService.findByProductCategory(Category.valueOf(category)));
        } catch (IllegalArgumentException e) {
            throw new ProductException("카테고리가 지정되지 않았습니다.", ProductErrorCode.NOT_EXIST_PRODUCT_CATEGORY);
        }

        return RsData.successOf(viewAllByProductCategory);
    }

    @GetMapping("/name/{name}")
    public RsData findByProductName(@PathVariable(name = "name") String name) {
        return RsData.successOf(productService.findProductsByProductName(name));
    }

    @GetMapping("/all")
    public RsData readPageByProduct(@RequestParam(name = "pageNumber", defaultValue = PAGE_START_NUMBER) Integer page) {
        Pageable pageRequest = PageRequest.of(page - 1, 5);
        Page<ViewAllResponse> pageByProduct = productService.readPageByProduct(pageRequest);
        return RsData.successOf(new PageResponse<>(pageByProduct));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RsData createProduct(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute @Valid CreateRequest createRequest,
                                @ModelAttribute(name = "images") MultipartFile images) throws IOException {
        productService.createProductWithImage(userDetails, createRequest, images);
        return RsData.of("S-1", POST_SUCCESS_PRODUCT, "create");
    }

    @PatchMapping("/{id}")
    public RsData updateProduct(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable(name = "id") Long productId,
                                @ModelAttribute @Valid UpdateRequest updateRequest) {
        productService.updateProduct(userDetails, productId, updateRequest);
        return RsData.of(MODIFY_SUCCESS_PRODUCT, "modify");
    }

    @DeleteMapping("/{id}")
    public RsData softDeleteProduct(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable(name = "id") Long productId) {
        productService.softDeleteProduct(userDetails, productId);
        return RsData.of(DELETE_SUCCESS_PRODUCT, "delete");
    }
}
