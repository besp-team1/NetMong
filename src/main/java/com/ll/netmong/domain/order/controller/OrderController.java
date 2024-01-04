package com.ll.netmong.domain.order.controller;

import com.ll.netmong.base.config.TossPaymentConfig;
import com.ll.netmong.common.RsData;
import com.ll.netmong.domain.order.dto.request.PaymentRequest;
import com.ll.netmong.domain.order.dto.response.PaymentFailResponse;
import com.ll.netmong.domain.order.dto.response.PaymentResponse;
import com.ll.netmong.domain.order.dto.response.PaymentSuccessResponse;
import com.ll.netmong.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/order")
public class OrderController {
    private final OrderService orderService;
    private final TossPaymentConfig tossPaymentConfig;

    @PostMapping
    public RsData requestPaymentByToss(@AuthenticationPrincipal UserDetails currentUser,
                                       @RequestBody PaymentRequest paymentRequest) throws Exception {
        PaymentResponse paymentResponse = orderService.requestPayment(paymentRequest.createOrder(), currentUser.getUsername());
        paymentResponse.setSuccessUrl(Optional.ofNullable(paymentRequest.getSuccessUrl()).orElse(tossPaymentConfig.getSuccessUrl()));
        paymentResponse.setFailUrl(Optional.ofNullable(paymentRequest.getFailUrl()).orElse(tossPaymentConfig.getFailUrl()));

        return RsData.successOf(paymentResponse);
    }

    @GetMapping("/success")
    public RsData successPaymentByToss(@RequestParam String paymentKey,
                                       @RequestParam String orderId,
                                       @RequestParam Long amount) {
        PaymentSuccessResponse paymentSuccessResponse = orderService.paymentSuccess(paymentKey, orderId, amount);

        return RsData.successOf(paymentSuccessResponse);
    }

    @GetMapping("/fail")
    public RsData failPaymentByToss(@RequestParam String code,
                                    @RequestParam String orderId,
                                    @RequestParam String message) {
        PaymentFailResponse paymentFailResponse = orderService.failPayment(code, orderId, message);

        return RsData.successOf(paymentFailResponse);
    }
}
