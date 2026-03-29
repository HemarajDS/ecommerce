package com.mercora.order.websocket;

import com.mercora.order.dto.OrderResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderStatusPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(OrderResponse orderResponse) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderResponse.id(), orderResponse);
    }
}
