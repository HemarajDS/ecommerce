package com.mercora.order.event;

import com.mercora.order.dto.PaymentSuccessEvent;
import com.mercora.order.model.OrderStatus;
import com.mercora.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessConsumer {

    private final OrderService orderService;

    public PaymentSuccessConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${mercora.order.payment-success-topic}", groupId = "order-service")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            orderService.confirmOrderByPaymentSession(event.paymentSessionId());
        }
    }
}
