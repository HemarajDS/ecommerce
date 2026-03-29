package com.mercora.payment.gateway;

import com.mercora.payment.dto.CreatePaymentRequest;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component("RAZORPAY")
public class RazorpayGatewayClient implements PaymentGatewayClient {

    @Override
    public String createPayment(CreatePaymentRequest request) {
        return "razorpay_" + UUID.randomUUID();
    }

    @Override
    public String refund(String gatewayReference, String reason) {
        return "razorpay_refund_" + UUID.randomUUID();
    }
}
