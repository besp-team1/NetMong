package com.ll.netmong.domain.productCart.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewCartResponse {
    private String productName;
    private String price;
    private Integer count;
}