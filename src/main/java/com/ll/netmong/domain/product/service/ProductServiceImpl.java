package com.ll.netmong.domain.product.service;

import com.ll.netmong.common.ProductException;
import com.ll.netmong.domain.image.service.ImageService;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import com.ll.netmong.domain.product.dto.request.CreateRequest;
import com.ll.netmong.domain.product.dto.request.UpdateRequest;
import com.ll.netmong.domain.product.dto.response.ViewAllResponse;
import com.ll.netmong.domain.product.dto.response.ViewSingleResponse;
import com.ll.netmong.domain.product.entity.Product;
import com.ll.netmong.domain.product.repository.ProductRepository;
import com.ll.netmong.domain.product.util.Category;
import com.ll.netmong.domain.product.util.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;

    @Override
    @Transactional
    public void createProductWithImage(UserDetails currentUser,
                                       CreateRequest createRequest, MultipartFile images) throws IOException {
        Product product = initProduct(currentUser, createRequest);
        if (isImageExists(images)) {
            product.addProductImage(imageService.uploadImage(product, images).orElseThrow());
        }
    }

    @Override
    public ViewSingleResponse findByProduct(Long productId) {
        return new ViewSingleResponse(validateExistProduct(productId));
    }

    @Override
    public List<ViewAllResponse> viewAllProducts() {
        return toViewAllResponse(productRepository.findAllWithImage());
    }

    @Override
    public List<ViewAllResponse> findByProductCategory(Category category) {
        return toViewAllResponse(productRepository.findByCategory(category));
    }

    @Override
    public List<ViewAllResponse> findProductsByProductName(String productName) {
        List<Product> products = productRepository.findByProductName(productName);
        if (products.isEmpty()) {
            throw new ProductException("존재 하지 않는 상품 이름 입니다.", ProductErrorCode.NOT_EXIST_PRODUCT_NAME);
        }

        return toViewAllResponse(products);
    }

    @Override
    public Page<ViewAllResponse> readPageByProduct(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllPageWithImage(pageable);
        return productPage.map(ViewAllResponse::pageByProduct);
    }

    @Override
    @Transactional
    public void updateProduct(UserDetails currentUser,
                              Long productId, UpdateRequest updateRequest) {
        Product findProduct = validateExistProduct(productId);
        validateCurrentUser(currentUser, findProduct);

        findProduct.modifyProduct(updateRequest);
    }

    @Override
    @Transactional
    public void softDeleteProduct(UserDetails currentUser,
                                  Long productId) {
        Product findProduct = validateExistProduct(productId);
        validateCurrentUser(currentUser, findProduct);

        productRepository.deleteById(productId);
    }

    public Product findProduct(Long productId) {
        return validateExistProduct(productId);
    }

    private Product initProduct(UserDetails currentUser, CreateRequest createRequest) {
        Member findMember = memberRepository.findByEmail(currentUser.getUsername()).orElseThrow(()
                -> new DataNotFoundException("사용자를 찾을 수 없습니다."));

        return productRepository.save(Product.builder()
                .productName(createRequest.getProductName())
                .price(createRequest.getPrice())
                .content(createRequest.getContent())
                .count(createRequest.getCount())
                .category(createRequest.getCategory())
                .member(findMember)
                .build());
    }

    private boolean isImageExists(MultipartFile image) {
        return !Objects.isNull(image);
    }

    private List<ViewAllResponse> toViewAllResponse(List<Product> products) {
        return products.stream()
                .filter(product -> product != null)
                .map(ViewAllResponse::new)
                .collect(Collectors.toList());
    }

    private void validateCurrentUser(UserDetails currentUser, Product findProduct) {
        if (!findProduct.getMember().getEmail().equals(currentUser.getUsername())) {
            throw new ProductException("등록한 사용자가 아니면 변경 할 수 없습니다.", ProductErrorCode.NOT_CHANGE_PRODUCT);
        }
    }

    private Product validateExistProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() ->
                new ProductException("상품이 존재하지 않습니다.", ProductErrorCode.NOT_EXIST_PRODUCT));
    }
}
