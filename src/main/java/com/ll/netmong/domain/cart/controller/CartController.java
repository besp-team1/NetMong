package com.ll.netmong.domain.cart.controller;

import com.ll.netmong.common.RsData;
import com.ll.netmong.domain.cart.dto.request.ProductCountRequest;
import com.ll.netmong.domain.cart.itemCart.service.ItemCartService;
import com.ll.netmong.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products/cart")
public class CartController {
    private static final String CART_SUCCESS_PRODUCT = "장바구니에 상품이 등록 되었습니다.";
    private final CartService cartService;
    private final ItemCartService itemCartService;

    @GetMapping
    public RsData readProductCartByUser(@AuthenticationPrincipal UserDetails userDetails) {
        String findMemberEmail = userDetails.getUsername();
        return RsData.successOf(itemCartService.readMemberCartByUser(findMemberEmail));
    }

    @PostMapping("{productId}")
    public RsData addMyCart(@AuthenticationPrincipal UserDetails currentUser,
                            @PathVariable(name = "productId") Long productId,
                            @RequestBody ProductCountRequest productCountRequest) {
        cartService.addProductByCart(currentUser, productId, productCountRequest);
        return RsData.of("S-1", CART_SUCCESS_PRODUCT, "create");
    }

    @DeleteMapping("{productId}")
    public RsData deleteByProduct(@AuthenticationPrincipal UserDetails currentUser,
                            @PathVariable(name = "productId") Long productId) {
        cartService.deleteByProduct(currentUser, productId);
        return RsData.successOf("delete");
    }
}
