package com.mercora.order.event;

import com.mercora.order.config.OrderProperties;
import com.mercora.order.model.OrderDocument;
import com.mercora.order.model.OrderStatus;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderProperties orderProperties;

    public OrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate, OrderProperties orderProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderProperties = orderProperties;
    }

    public void publishPlaced(OrderDocument order) {
        kafkaTemplate.send(orderProperties.getOrderPlacedTopic(), order.getId(), event("order.placed", order));
    }

    public void publishCancelled(OrderDocument order) {
        kafkaTemplate.send(orderProperties.getOrderCancelledTopic(), order.getId(), event("order.cancelled", order));
    }

    public void publishDelivered(OrderDocument order) {
        kafkaTemplate.send(orderProperties.getOrderDeliveredTopic(), order.getId(), event("order.delivered", order));
    }

    private OrderEvent event(String type, OrderDocument order) {
        List<OrderEventItem> items = order.getItems().stream()
                .map(item -> new OrderEventItem(item.getProductId(), item.getSku(), item.getQuantity(), "DEFAULT"))
                .toList();
        return new OrderEvent(
                type,
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus().name(),
                items,
                java.time.Instant.now());
    }
}
