package com.ll.netmong.domain.cart.itemCart.service;

import com.ll.netmong.domain.cart.dto.request.ProductCountRequest;
import com.ll.netmong.domain.cart.dto.response.ViewCartResponse;
import com.ll.netmong.domain.cart.entity.Cart;
import com.ll.netmong.domain.cart.itemCart.entity.ItemCart;

import java.util.List;

public interface ItemCartService {
    List<ViewCartResponse> readMemberCartByUser(String findMemberName);

    void addToCartForNewProduct(Cart cart, Long productId, ProductCountRequest productCountRequest);

    void addToCartForExistingProduct(ItemCart findItemCart, Cart cart, ProductCountRequest productCountRequest);

    void deleteByProduct(Cart cart, Long productId);

    ItemCart getItemCart(Cart cart, Long productId);

    String findMemberEmailByProductId(Long productId);
}
