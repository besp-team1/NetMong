package com.ll.netmong.domain.order.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSuccessResponse {
    private String mid; // 가맹점 Id -> tosspayments
    private String version; // Payment 객체 응답 버전
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String currency; // "KRW"
    private String method; // 결제 수단
    private String totalAmount;
    private String balanceAmount;
    private String suppliedAmount;
    private String vat; // 부가가치세
    private String status; // 결제 처리 상태
    private String requestedAt;
    private String approvedAt;
    private String useEscrow; // false
    private String cultureExpense; // false
//    private PaymentSuccessCardDto card; // 결제 카드 정보 (아래 자세한 정보 있음)
    private String type; // 결제 타입 정보 (NOMAL / BILLING / CONNECTPAY)
}
