package com.ll.netmong.domain.cart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCountRequest {
    @Min(value = 1, message = "상품의 수량은 최소 1개 이상이어야 합니다.")
    @Max(value = 30, message = "상품의 수량은 최대 30개까지 가능합니다.")
    private Integer count;
}
