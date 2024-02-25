package com.ll.netmong.domain.cart.entity;

import com.ll.netmong.common.BaseEntity;
import com.ll.netmong.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "product_cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "item_count_in_cart")
    private Integer totalCount;

    public static Cart createCart(Member member) {
        return Cart.builder()
                .totalCount(0)
                .member(member)
                .build();
    }

    public void addCount(Integer count) {
        this.totalCount += count;
    }

    public void minusCount(Integer count){
        this.totalCount -= count;
    }
}
