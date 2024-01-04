package com.ll.netmong.domain.order.entity;

import com.ll.netmong.common.BaseEntity;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.order.dto.response.PaymentResponse;
import com.ll.netmong.domain.order.util.PayType;
import com.ll.netmong.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "pay_type")
    @Enumerated(EnumType.STRING)
    private PayType payType;

    @Column(name = "pay_amount")
    private Long amount;

    @Column(name = "pay_name")
    private String orderName;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "pay_success_status")
    private boolean paySuccessYN;

    @Column(name = "cancel_status")
    private boolean cancelYN;

    @Column(name = "fail_reason")
    private String failReason;


    public void setMember(Member member) {
        this.member = member;
    }


    public void paymentSuccess(String paymentKey) {
        this.paymentKey = paymentKey;
        this.paySuccessYN = true;
    }

    public void paymentFail(String message) {
        this.failReason = message;
        this.paySuccessYN = false;
    }

    public PaymentResponse toPaymentResponse() {
        return PaymentResponse
                .builder()
                .payType(payType.getPaymentType())
                .amount(amount)
                .orderName(orderName)
                .orderId(orderId)
                .customerEmail(member.getEmail())
                .customerName(member.getRealName())
                .createdAt(String.valueOf(getCreateDate()))
                .cancelYN(cancelYN)
                .failReason(failReason)
                .build();
    }
}
