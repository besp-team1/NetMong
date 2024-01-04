package com.ll.netmong.domain.order.util;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PayType {
    CARD("카드"),
    CASH("현금"),
    POINT("포인트");

    PayType(String paymentType) {
        this.paymentType = paymentType;
    }

    private final String paymentType;

    @JsonValue
    public String getPaymentType() {
        return paymentType;
    }
}
