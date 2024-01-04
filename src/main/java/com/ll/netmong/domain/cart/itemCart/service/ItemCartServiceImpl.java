package com.ll.netmong.domain.cart.itemCart.service;

import com.ll.netmong.common.ProductException;
import com.ll.netmong.domain.cart.dto.request.ProductCountRequest;
import com.ll.netmong.domain.cart.dto.response.ViewCartResponse;
import com.ll.netmong.domain.cart.entity.Cart;
import com.ll.netmong.domain.cart.itemCart.entity.ItemCart;
import com.ll.netmong.domain.cart.itemCart.repository.ItemCartRepository;
import com.ll.netmong.domain.product.entity.Product;
import com.ll.netmong.domain.product.repository.ProductRepository;
import com.ll.netmong.domain.product.service.ProductServiceImpl;
import com.ll.netmong.domain.product.util.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemCartServiceImpl implements ItemCartService {
    private static final int MINIMUM_STOCK_COUNT = 0;
    private final ItemCartRepository itemCartRepository;
    private final ProductRepository productRepository;
    private final ProductServiceImpl productService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<ViewCartResponse> readMemberCartByUser(String findMemberEmail) {
        List<ItemCart> findItemCart = itemCartRepository.findAll();
        List<ViewCartResponse> memberProducts = new ArrayList<>();

        for (ItemCart itemCart : findItemCart) {
            if (itemCart.getCart().getMember().getEmail().equals(findMemberEmail)) {
                ViewCartResponse viewCartResponse = new ViewCartResponse();

                viewCartResponse.setProductId(itemCart.getProduct().getId());
                viewCartResponse.setProductName(itemCart.getProduct().getProductName());
                viewCartResponse.setPrice(itemCart.getProduct().getPrice());
                viewCartResponse.setCount(itemCart.getStackCount());
                viewCartResponse.setImageUrl(itemCart.getImageUrl());

                memberProducts.add(viewCartResponse);
            }
        }
        return memberProducts;
    }


    @Override
    public ItemCart getItemCart(Cart cart, Long productId) {
        return itemCartRepository.findByCartIdAndProductId(cart.getId(), productId);
    }

    @Override
    public void addToCartForNewProduct(Cart cart, Long productId, ProductCountRequest productCountRequest) {
        Product product = productService.findProduct(productId);
        removeStock(productId, productCountRequest.getCount());
        cart.addCount(productCountRequest.getCount());
        ItemCart itemCart = ItemCart.createItemCart(cart, product, productCountRequest.getCount());
        itemCartRepository.save(itemCart);
    }

    @Override
    public void addToCartForExistingProduct(ItemCart findItemCart, Cart cart, ProductCountRequest productCountRequest) {
        Long productId = findItemCart.getProduct().getId();
        removeStock(productId, productCountRequest.getCount());
        cart.addCount(productCountRequest.getCount());
        findItemCart.addCount(productCountRequest.getCount());
    }

    @Override
    public void deleteByProduct(Cart cart, Long productId) {
        ItemCart itemCart = getItemCart(cart, productId);
        int stackCount = itemCart.getStackCount();

        Product product = productRepository.findById(itemCart.getProduct().getId())
                .orElseThrow(() -> new ProductException("존재하지 않는 상품입니다.", ProductErrorCode.NOT_EXIST_PRODUCT));
        product.setCount(product.getCount() + stackCount);

        cart.minusCount(stackCount);
        itemCartRepository.deleteById(itemCart.getId());
        productRepository.save(product);
    }

    @Transactional
    public void removeStock(Long productId, Integer count) {
        try {
            transactionTemplate.execute(status -> {
                // Pessimistic Lock을 걸고 상품을 조회합니다.
                Product findByProduct = productRepository.findByIdWithPessimisticLock(productId)
                        .orElseThrow(() -> new ProductException("존재하지 않는 상품입니다.", ProductErrorCode.NOT_EXIST_PRODUCT));

                int restStock = findByProduct.getCount() - count;

                // 재고가 충분하지 않다면, 예외를 발생시킵니다.
                validateRestStockIsEnough(restStock);

                // 재고가 충분하다면, 상품을 업데이트하고 저장합니다.
                findByProduct.setCount(restStock);
                productRepository.save(findByProduct);

                return null;
            });
        } catch (CannotAcquireLockException | TransactionTimedOutException e) {
            // 데드락이 발생하면 재귀적으로 메소드를 다시 호출하여 재시도합니다.
            removeStock(productId, count);
        }
    }

    private void validateRestStockIsEnough(int restStock) {
        if (restStock < MINIMUM_STOCK_COUNT) {
            throw new ProductException("남아있는 재고가 부족합니다.", ProductErrorCode.NOT_ENOUGH_PRODUCT_STOCK);
        }
    }

    @Override
    public String findMemberEmailByProductId(Long productId) {
        Product findProduct = productService.findProduct(productId);
        return findProduct.getMember().getEmail();
    }
}
