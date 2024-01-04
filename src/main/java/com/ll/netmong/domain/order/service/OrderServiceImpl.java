package com.ll.netmong.domain.order.service;

import com.ll.netmong.base.config.TossPaymentConfig;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.service.MemberService;
import com.ll.netmong.domain.order.dto.response.PaymentFailResponse;
import com.ll.netmong.domain.order.dto.response.PaymentResponse;
import com.ll.netmong.domain.order.dto.response.PaymentSuccessResponse;
import com.ll.netmong.domain.order.entity.Order;
import com.ll.netmong.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final MemberService memberService;
    private final OrderRepository orderRepository;
    private final TossPaymentConfig tossPaymentConfig;

    @Override
    @Transactional
    public PaymentResponse requestPayment(Order order, String userEmail) throws Exception {
        Member findMemberByEmail = memberService.findByEmail(userEmail);
        order.setMember(findMemberByEmail);

        return orderRepository.save(order).toPaymentResponse();
    }

    @Override
    @Transactional
    public PaymentSuccessResponse paymentSuccess(String paymentKey, String orderId, Long amount) {
        Order order = verifyPayment(orderId, amount); // 요청가격 == 결제된 금액
        PaymentSuccessResponse result = requestPaymentAccept(paymentKey, orderId, amount);
        order.paymentSuccess(paymentKey);

        // todo : 회원의 포인트 갱신으로 인해 Member 테이블에 Point 컬럼 추가
//        order.getMember().

        return result;
    }

    @Override
    @Transactional
    public PaymentFailResponse failPayment(String code, String orderId, String message) {
        Order order = findByOrder(orderId);
        order.paymentFail(message);

        return requestPaymentFail(code, message, orderId);
    }

    private PaymentFailResponse requestPaymentFail(String code, String message, String orderId) {
        return PaymentFailResponse
                .builder()
                .errorCode(code)
                .errorMessage(message)
                .orderId(orderId)
                .build();
    }

    private Order verifyPayment(String orderId, Long amount) {
        Order order = findByOrder(orderId);

        if (!order.getAmount().equals(amount)) {
            throw new IllegalArgumentException();
        }
        return order;
    }

    private Order findByOrder(String orderId) {
        return orderRepository.findByOrderId(orderId).orElseThrow(() -> {
            throw new IllegalArgumentException();
        });
    }

    private PaymentSuccessResponse requestPaymentAccept(String paymentKey, String orderId, Long amount) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();
        JSONObject params = createParmas(orderId, amount);

        PaymentSuccessResponse result;

        try {
            result = restTemplate.postForObject(TossPaymentConfig.TOSS_COMMON_URL + paymentKey,
                    new HttpEntity<>(params, headers),
                    PaymentSuccessResponse.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }

        return result;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // 토스에서 받은 시크릿 키를 Base64를 이용하여 인코딩 한다.
        // 이때, {시크릿 키 + ":"} 조합으로 인코딩 해야함
        String encodeAuthKey = new String(
                Base64.getEncoder().encode((tossPaymentConfig.getTestSecretKey() + ":").getBytes(StandardCharsets.UTF_8))
        );

        headers.setBasicAuth(encodeAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return headers;
    }

    private JSONObject createParmas(String orderId, Long amount) {
        JSONObject params = new JSONObject();
        params.put("orderId", orderId);
        params.put("amount", amount);
        return params;
    }
}
