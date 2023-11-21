package com.ll.netmong.domain.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequest {
    private String productName;
    private String price;
    private String content;
}
