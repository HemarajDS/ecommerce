package com.mercora.payment.gateway;

import com.mercora.payment.dto.CreatePaymentRequest;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component("STRIPE")
public class StripeGatewayClient implements PaymentGatewayClient {

    @Override
    public String createPayment(CreatePaymentRequest request) {
        return "stripe_" + UUID.randomUUID();
    }

    @Override
    public String refund(String gatewayReference, String reason) {
        return "stripe_refund_" + UUID.randomUUID();
    }
}
