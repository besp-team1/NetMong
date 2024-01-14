package com.ll.netmong.domain.cart.itemCart.repository;

import com.ll.netmong.domain.cart.itemCart.entity.ItemCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemCartRepository extends JpaRepository<ItemCart, Long> {
    ItemCart findByCartIdAndProductId(Long cartId, Long productId);

    @Query("select ic from ItemCart ic join fetch ic.cart c join fetch c.member m join fetch ic.product where m.email = :email")
    List<ItemCart> findByMemberEmail(@Param("email")String findMemberEmail);
}
