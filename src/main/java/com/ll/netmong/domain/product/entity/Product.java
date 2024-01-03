package com.ll.netmong.domain.product.entity;

import com.ll.netmong.common.BaseEntity;
import com.ll.netmong.domain.image.entity.Image;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.product.dto.request.UpdateRequest;
import com.ll.netmong.domain.product.util.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@DynamicInsert
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Where(clause = "status = 'Y'")
@SQLDelete(sql = "UPDATE product SET status = 'N' where id = ?")
public class Product extends BaseEntity {
    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private String price;

    @Lob
    @Column(name = "product_content")
    private String content;

    @Column(name = "product_count")
    @Setter
    private Integer count;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    private Category category;

    @Builder.Default
    @Column(nullable = false)
    private String status = "Y";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    public static Image createProductImage(String imageUrl, String s3ImageUrl) {
        return new Image(imageUrl, s3ImageUrl);
    }

    public void addProductImage(Image productImage) {
        this.image = productImage;
    }

    public void modifyProduct(UpdateRequest updateRequest) {
        this.productName = updateRequest.getProductName();
        this.price = updateRequest.getPrice();
        this.count = updateRequest.getCount();
        this.content = updateRequest.getContent();
        this.category = updateRequest.getCategory();
    }
}
