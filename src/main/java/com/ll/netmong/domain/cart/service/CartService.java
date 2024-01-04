package com.ll.netmong.domain.cart.service;

import com.ll.netmong.domain.cart.dto.request.ProductCountRequest;
import com.ll.netmong.domain.member.entity.Member;
import org.springframework.security.core.userdetails.UserDetails;

public interface CartService {
    void createCart(Member member);

    void addProductByCart(UserDetails currentUser, Long productId, ProductCountRequest productCountRequest);

    void deleteByProduct(UserDetails currentUser, Long productId);
}
