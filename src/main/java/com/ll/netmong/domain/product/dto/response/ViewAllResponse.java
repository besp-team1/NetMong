package com.ll.netmong.domain.product.dto.response;

import com.ll.netmong.domain.product.entity.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewAllResponse {
    private String productName;
    private String price;
    private String content;

    public ViewAllResponse(Product product) {
        this.productName = product.getProductName();
        this.price = product.getPrice();
        this.content = product.getContent();
    }
}
