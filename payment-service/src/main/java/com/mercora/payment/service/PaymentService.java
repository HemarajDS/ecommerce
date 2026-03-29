package com.mercora.payment.service;

import com.mercora.payment.dto.CreatePaymentRequest;
import com.mercora.payment.dto.PaymentResponse;
import com.mercora.payment.dto.PaymentWebhookRequest;
import com.mercora.payment.dto.RefundRequest;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse processWebhook(PaymentWebhookRequest request);

    PaymentResponse refund(String paymentSessionId, RefundRequest request);

    PaymentResponse getPayment(String paymentSessionId);
}
