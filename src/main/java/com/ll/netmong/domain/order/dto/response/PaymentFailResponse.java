package com.ll.netmong.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PaymentFailResponse {
    private String errorCode;
    private String errorMessage;
    private String orderId;
}
