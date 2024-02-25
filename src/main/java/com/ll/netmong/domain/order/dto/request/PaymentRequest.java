package com.ll.netmong.domain.order.dto.request;

import com.ll.netmong.domain.order.entity.Order;
import com.ll.netmong.domain.order.util.PayType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {
    private PayType payType;
    private String orderName; // 주문명, ex : 포인트 충전
    private Long amount;
    private String successUrl; // 성공 시 리다이렉트 될 URL
    private String failUrl; // 실패 시 리다이렉트 될 URL

    public Order createOrder() {
        return Order
                .builder()
                .payType(payType)
                .amount(amount)
                .orderName(orderName)
                .orderId(UUID.randomUUID().toString())
                .paySuccessYN(false)
                .build();
    }
}
