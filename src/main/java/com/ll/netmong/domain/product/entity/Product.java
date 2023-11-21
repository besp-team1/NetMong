package com.ll.netmong.domain.product.entity;

import com.ll.netmong.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class Product extends BaseEntity {
    @Column(name = "product_name")
    private String productName;
    @Column(name = "product_price")
    private String price;
    @Lob
    @Column(name = "product_content")
    private String content;
}
