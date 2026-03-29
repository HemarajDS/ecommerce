package com.mercora.payment.controller;

import com.mercora.payment.dto.CreatePaymentRequest;
import com.mercora.payment.dto.PaymentResponse;
import com.mercora.payment.dto.PaymentWebhookRequest;
import com.mercora.payment.dto.RefundRequest;
import com.mercora.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment intent, webhook, and refund APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a payment intent")
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Process a payment provider webhook")
    public PaymentResponse webhook(@Valid @RequestBody PaymentWebhookRequest request) {
        return paymentService.processWebhook(request);
    }

    @PostMapping("/{paymentSessionId}/refund")
    @Operation(summary = "Refund a successful payment")
    public PaymentResponse refund(@PathVariable String paymentSessionId, @Valid @RequestBody RefundRequest request) {
        return paymentService.refund(paymentSessionId, request);
    }

    @GetMapping("/{paymentSessionId}")
    @Operation(summary = "Get payment by session ID")
    public PaymentResponse get(@PathVariable String paymentSessionId) {
        return paymentService.getPayment(paymentSessionId);
    }
}
