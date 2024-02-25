package com.ll.netmong.base.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TossPaymentConfig {
    public static final String TOSS_COMMON_URL = "https://api.tosspayments.com/v1/payments/";

    @Value("${payment.toss.test-client-api-key}")
    private String testClientApiKey;

    @Value("${payment.toss.test-secrete-api-key}")
    private String testSecretKey;

    /**
     * 실제 전자 결제 신청 후 사용할 API 키
     * @Value("${payment.toss.live_client_api_key}")
     * private String liveClientApiKey;
     * @Value("${payment.toss.live_secrete_api_key}")
     * private String liveSecretKey;
     */

    @Value("${payment.success-url}")
    private String successUrl;

    @Value("${payment.fail-url}")
    private String failUrl;
}
