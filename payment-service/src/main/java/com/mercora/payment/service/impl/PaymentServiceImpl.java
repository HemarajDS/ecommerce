package com.mercora.payment.service.impl;

import com.mercora.payment.config.PaymentProperties;
import com.mercora.payment.dto.CreatePaymentRequest;
import com.mercora.payment.dto.PaymentResponse;
import com.mercora.payment.dto.PaymentWebhookRequest;
import com.mercora.payment.dto.RefundRequest;
import com.mercora.payment.event.PaymentEventPublisher;
import com.mercora.payment.exception.BusinessRuleException;
import com.mercora.payment.exception.ResourceNotFoundException;
import com.mercora.payment.factory.PaymentGatewayFactory;
import com.mercora.payment.gateway.PaymentGatewayClient;
import com.mercora.payment.model.PaymentDocument;
import com.mercora.payment.model.PaymentProvider;
import com.mercora.payment.model.PaymentStatus;
import com.mercora.payment.repository.PaymentRepository;
import com.mercora.payment.service.PaymentService;
import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final StringRedisTemplate redisTemplate;
    private final PaymentProperties paymentProperties;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentGatewayFactory paymentGatewayFactory,
            StringRedisTemplate redisTemplate,
            PaymentProperties paymentProperties,
            PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.redisTemplate = redisTemplate;
        this.paymentProperties = paymentProperties;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        String key = idempotencyKey(request.idempotencyKey());
        if (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(
                key, request.paymentSessionId(), Duration.ofHours(paymentProperties.getIdempotencyTtlHours())))) {
            return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(this::toResponse)
                    .orElseThrow(() -> new BusinessRuleException("Duplicate payment request detected"));
        }

        PaymentProvider provider = PaymentProvider.valueOf(paymentProperties.getProvider().toUpperCase());
        PaymentGatewayClient gatewayClient = paymentGatewayFactory.getGateway(provider);

        PaymentDocument payment = new PaymentDocument();
        payment.setPaymentSessionId(request.paymentSessionId());
        payment.setOrderId(request.orderId());
        payment.setUserId(request.userId());
        payment.setProvider(provider);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setGatewayReference(gatewayClient.createPayment(request));
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse processWebhook(PaymentWebhookRequest request) {
        String webhookKey = idempotencyKey("webhook:" + request.idempotencyKey());
        if (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(
                webhookKey, request.paymentSessionId(), Duration.ofHours(paymentProperties.getIdempotencyTtlHours())))) {
            return paymentRepository.findByPaymentSessionId(request.paymentSessionId())
                    .map(this::toResponse)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        }

        PaymentDocument payment = paymentRepository.findByPaymentSessionId(request.paymentSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setGatewayReference(request.gatewayReference());
        payment.setStatus(resolveStatus(request.status()));
        payment.setUpdatedAt(Instant.now());
        PaymentDocument saved = paymentRepository.save(payment);
        paymentEventPublisher.publish(saved);
        return toResponse(saved);
    }

    @Override
    public PaymentResponse refund(String paymentSessionId, RefundRequest request) {
        PaymentDocument payment = paymentRepository.findByPaymentSessionId(paymentSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessRuleException("Only successful payments can be refunded");
        }

        PaymentGatewayClient gatewayClient = paymentGatewayFactory.getGateway(payment.getProvider());
        payment.setGatewayReference(gatewayClient.refund(payment.getGatewayReference(), request.reason()));
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(Instant.now());
        PaymentDocument saved = paymentRepository.save(payment);
        paymentEventPublisher.publish(saved);
        return toResponse(saved);
    }

    @Override
    public PaymentResponse getPayment(String paymentSessionId) {
        return paymentRepository.findByPaymentSessionId(paymentSessionId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private PaymentStatus resolveStatus(String status) {
        return switch (status.toUpperCase()) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "FAILED" -> PaymentStatus.FAILED;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            default -> throw new BusinessRuleException("Unsupported payment status: " + status);
        };
    }

    private String idempotencyKey(String key) {
        return "payment:idempotency:" + key;
    }

    private PaymentResponse toResponse(PaymentDocument payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentSessionId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getGatewayReference(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
