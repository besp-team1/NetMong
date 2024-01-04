package com.ll.netmong.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String payType;
    private Long amount;
    private String orderName;
    private String orderId;
    private String customerEmail;
    private String customerName;
    private String successUrl;
    private String failUrl;
    private String failReason;
    private boolean cancelYN;
    private String cancelReason;
    private String createdAt;
}
