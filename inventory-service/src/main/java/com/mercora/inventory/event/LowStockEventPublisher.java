package com.mercora.inventory.event;

import com.mercora.inventory.config.InventoryProperties;
import com.mercora.inventory.model.InventoryItemDocument;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LowStockEventPublisher {

    private final KafkaTemplate<String, LowStockEvent> kafkaTemplate;
    private final InventoryProperties inventoryProperties;

    public LowStockEventPublisher(KafkaTemplate<String, LowStockEvent> kafkaTemplate, InventoryProperties inventoryProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryProperties = inventoryProperties;
    }

    public void publishIfNeeded(InventoryItemDocument inventoryItem) {
        if (inventoryItem.getAvailableQuantity() <= inventoryItem.getSafetyStock()) {
            LowStockEvent event = new LowStockEvent(
                    inventoryItem.getProductId(),
                    inventoryItem.getSku(),
                    inventoryItem.getWarehouseCode(),
                    inventoryItem.getAvailableQuantity(),
                    inventoryItem.getSafetyStock());
            kafkaTemplate.send(inventoryProperties.getLowStockTopic(), inventoryItem.getProductId(), event);
        }
    }
}
