package com.ll.netmong.domain.cart.controller;

import com.ll.netmong.common.RsData;
import com.ll.netmong.domain.cart.dto.request.ProductCountRequest;
import com.ll.netmong.domain.cart.dto.response.ViewCartResponse;
import com.ll.netmong.domain.cart.itemCart.service.ItemCartService;
import com.ll.netmong.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products/cart")
public class CartController {
    private static final String CART_SUCCESS_PRODUCT = "장바구니에 상품이 등록 되었습니다.";
    private final CartService cartService;
    private final ItemCartService itemCartService;

    @GetMapping
    public ResponseEntity<RsData> readCartByUser(@AuthenticationPrincipal UserDetails userDetails) {
        String findMemberEmail = userDetails.getUsername();
        RsData<List<ViewCartResponse>> responseBody = RsData.successOf(itemCartService.readMemberCartByUser(findMemberEmail));
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("{productId}")
    public ResponseEntity<RsData> addProductToCart(@AuthenticationPrincipal UserDetails currentUser,
                            @PathVariable(name = "productId") Long productId,
                            @RequestBody ProductCountRequest productCountRequest) {
        cartService.addProductByCart(currentUser, productId, productCountRequest);
        RsData<String> responseBody = RsData.of("S-1", CART_SUCCESS_PRODUCT, "create");
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);

    }

    @DeleteMapping("{productId}")
    public ResponseEntity<RsData> removeProductFromCart(@AuthenticationPrincipal UserDetails currentUser,
                            @PathVariable(name = "productId") Long productId) {
        cartService.deleteByProduct(currentUser, productId);
        RsData responseBody = RsData.successOf("delete");
        return ResponseEntity.ok(responseBody);
    }
}
