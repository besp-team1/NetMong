package com.ll.netmong.domain.order.service;

import com.ll.netmong.domain.order.dto.response.PaymentFailResponse;
import com.ll.netmong.domain.order.dto.response.PaymentResponse;
import com.ll.netmong.domain.order.dto.response.PaymentSuccessResponse;
import com.ll.netmong.domain.order.entity.Order;

public interface OrderService {

    PaymentResponse requestPayment(Order order, String userEmail) throws Exception;

    PaymentSuccessResponse paymentSuccess(String paymentKey, String orderId, Long amount);

    PaymentFailResponse failPayment(String code, String orderId, String message);
}
