package com.mercora.payment.gateway;

import com.mercora.payment.dto.CreatePaymentRequest;

public interface PaymentGatewayClient {

    String createPayment(CreatePaymentRequest request);

    String refund(String gatewayReference, String reason);
}
