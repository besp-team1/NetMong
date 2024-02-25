package com.ll.netmong.domain.cart.service;

import com.ll.netmong.common.ProductException;
import com.ll.netmong.domain.cart.dto.request.ProductCountRequest;
import com.ll.netmong.domain.cart.entity.Cart;
import com.ll.netmong.domain.cart.itemCart.entity.ItemCart;
import com.ll.netmong.domain.cart.itemCart.service.ItemCartService;
import com.ll.netmong.domain.cart.repository.CartRepository;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.product.util.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ItemCartService itemCartService;

    @Override
    @Transactional
    public void createCart(Member member) {
        Cart cart = Cart.createCart(member);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void addProductByCart(UserDetails currentUser, Long productId, ProductCountRequest productCountRequest) {
        Cart cart = validateExistMember(currentUser);
        validateSelfAddProduct(currentUser, productId);

        Optional<ItemCart> findItemCart = Optional.ofNullable(itemCartService.getItemCart(cart, productId));

        if (findItemCart.isEmpty()) {
            itemCartService.addToCartForNewProduct(cart, productId, productCountRequest);
            return;
        }
        itemCartService.addToCartForExistingProduct(findItemCart.get(), cart, productCountRequest);
    }

    @Override
    @Transactional
    public void deleteByProduct(UserDetails currentUser, Long productId) {
        Cart cart = validateExistMember(currentUser);
        itemCartService.deleteByProduct(cart, productId);
    }

    private Cart validateExistMember(UserDetails currentUser) {
        return cartRepository.findByMemberEmail(currentUser.getUsername())
                .orElseThrow(() -> new ProductException("회원이 존재하지 않습니다.", ProductErrorCode.NOT_EXIST_PRODUCT));
    }

    private void validateSelfAddProduct(UserDetails currentUser, Long productId) {
        if (itemCartService.findMemberEmailByProductId(productId).equals(currentUser.getUsername())) {
            throw new ProductException("본인이 등록한 상품은 장바구니에 담을 수 없습니다.",
                    ProductErrorCode.NOT_SELF_ADD_PRODUCT_CART);
        }
    }
}
