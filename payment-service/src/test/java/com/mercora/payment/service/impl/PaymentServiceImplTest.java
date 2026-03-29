package com.mercora.payment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.payment.config.PaymentProperties;
import com.mercora.payment.dto.CreatePaymentRequest;
import com.mercora.payment.dto.PaymentWebhookRequest;
import com.mercora.payment.dto.RefundRequest;
import com.mercora.payment.event.PaymentEventPublisher;
import com.mercora.payment.exception.BusinessRuleException;
import com.mercora.payment.factory.PaymentGatewayFactory;
import com.mercora.payment.gateway.PaymentGatewayClient;
import com.mercora.payment.model.PaymentDocument;
import com.mercora.payment.model.PaymentProvider;
import com.mercora.payment.model.PaymentStatus;
import com.mercora.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGatewayFactory paymentGatewayFactory;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private PaymentEventPublisher paymentEventPublisher;
    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentProperties paymentProperties;

    @BeforeEach
    void setUp() {
        paymentProperties = new PaymentProperties();
        paymentProperties.setProvider("RAZORPAY");
        paymentProperties.setIdempotencyTtlHours(24);
        paymentProperties.setPaymentSuccessTopic("payment.success");
        paymentProperties.setPaymentFailedTopic("payment.failed");
        paymentProperties.setPaymentRefundedTopic("payment.refunded");
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                paymentGatewayFactory,
                redisTemplate,
                paymentProperties,
                paymentEventPublisher);
    }

    @Test
    void createPaymentShouldPersistInitiatedPayment() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(true);
        when(paymentGatewayFactory.getGateway(PaymentProvider.RAZORPAY)).thenReturn(paymentGatewayClient);
        when(paymentGatewayClient.createPayment(any())).thenReturn("razorpay_ref");
        when(paymentRepository.save(any(PaymentDocument.class))).thenAnswer(invocation -> {
            PaymentDocument payment = invocation.getArgument(0);
            payment.setId("payment-1");
            return payment;
        });

        var response = paymentService.createPayment(new CreatePaymentRequest(
                "order-1",
                "user-1",
                "session-1",
                BigDecimal.valueOf(1999),
                "INR",
                "idem-1"));

        assertEquals(PaymentStatus.INITIATED, response.status());
    }

    @Test
    void refundShouldRejectNonSuccessfulPayments() {
        PaymentDocument payment = new PaymentDocument();
        payment.setPaymentSessionId("session-1");
        payment.setStatus(PaymentStatus.INITIATED);
        when(paymentRepository.findByPaymentSessionId("session-1")).thenReturn(Optional.of(payment));

        assertThrows(BusinessRuleException.class, () -> paymentService.refund("session-1", new RefundRequest("Duplicate")));
    }
}
