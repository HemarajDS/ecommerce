package com.mercora.notification.event;

import com.mercora.notification.dto.DealerEventMessage;
import com.mercora.notification.dto.LowStockEventMessage;
import com.mercora.notification.dto.OrderEventMessage;
import com.mercora.notification.dto.PaymentEventMessage;
import com.mercora.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PlatformEventConsumer {

    private final NotificationService notificationService;

    public PlatformEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = {"order.placed", "order.cancelled", "order.delivered"}, groupId = "notification-service")
    public void handleOrderEvent(OrderEventMessage event) {
        if (event.userId() != null) {
            notificationService.notifyUser(
                    event.userId(),
                    "Order update: " + event.orderNumber(),
                    "Your order is now " + event.status(),
                    event.eventType());
        }
    }

    @KafkaListener(topics = {"payment.success", "payment.failed", "payment.refunded"}, groupId = "notification-service")
    public void handlePaymentEvent(PaymentEventMessage event) {
        notificationService.notifyUser(
                "finance",
                "Payment update",
                "Payment event " + event.eventType() + " received for order " + event.orderId(),
                event.eventType());
    }

    @KafkaListener(topics = {"inventory.low"}, groupId = "notification-service")
    public void handleLowStock(LowStockEventMessage event) {
        notificationService.notifyUser(
                "operations",
                "Low stock alert",
                "SKU " + event.sku() + " at warehouse " + event.warehouseCode() + " is below safety stock.",
                "inventory.low");
    }

    @KafkaListener(topics = {"dealer.created", "dealer.po.pending-approval"}, groupId = "notification-service")
    public void handleDealerEvent(DealerEventMessage event) {
        notificationService.notifyUser(
                "dealer-admin",
                "Dealer workflow update",
                "Dealer event " + event.eventType() + " received with reference " + event.referenceId(),
                event.eventType());
    }
}
