package com.mercora.inventory.event;

import com.mercora.inventory.dto.OrderPlacedEvent;
import com.mercora.inventory.dto.OrderPlacedItem;
import com.mercora.inventory.dto.ReserveStockRequest;
import com.mercora.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private final InventoryService inventoryService;

    public OrderPlacedConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "${mercora.inventory.order-placed-topic}", groupId = "inventory-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (event.items() == null) {
            return;
        }
        for (OrderPlacedItem item : event.items()) {
            inventoryService.reserveStock(new ReserveStockRequest(
                    event.orderId(),
                    item.productId(),
                    item.sku(),
                    item.warehouseCode() == null || item.warehouseCode().isBlank() ? "DEFAULT" : item.warehouseCode(),
                    item.quantity()));
        }
    }
}
