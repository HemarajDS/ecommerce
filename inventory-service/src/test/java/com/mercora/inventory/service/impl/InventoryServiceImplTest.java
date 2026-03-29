package com.mercora.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercora.inventory.config.InventoryProperties;
import com.mercora.inventory.dto.InventoryBatchRequest;
import com.mercora.inventory.dto.ReserveStockRequest;
import com.mercora.inventory.dto.StockAdjustmentRequest;
import com.mercora.inventory.event.LowStockEventPublisher;
import com.mercora.inventory.exception.BusinessRuleException;
import com.mercora.inventory.model.InventoryBatch;
import com.mercora.inventory.model.InventoryItemDocument;
import com.mercora.inventory.model.StockAdjustmentType;
import com.mercora.inventory.repository.InventoryItemRepository;
import com.mercora.inventory.repository.InventoryReservationRepository;
import com.mercora.inventory.repository.StockAdjustmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private InventoryReservationRepository inventoryReservationRepository;
    @Mock
    private StockAdjustmentRepository stockAdjustmentRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private LowStockEventPublisher lowStockEventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryProperties inventoryProperties;

    @BeforeEach
    void setUp() {
        inventoryProperties = new InventoryProperties();
        inventoryProperties.setReservationTtlMinutes(15);
        inventoryProperties.setLockTtlSeconds(15);
        inventoryProperties.setDefaultSafetyStock(5);
        inventoryService = new InventoryServiceImpl(
                inventoryItemRepository,
                inventoryReservationRepository,
                stockAdjustmentRepository,
                redisTemplate,
                inventoryProperties,
                lowStockEventPublisher);
    }

    @Test
    void reserveStockShouldRejectInsufficientInventory() {
        InventoryItemDocument item = new InventoryItemDocument();
        item.setId("inv-1");
        item.setProductId("prod-1");
        item.setWarehouseCode("BLR-1");
        item.setAvailableQuantity(1);
        item.setReservedQuantity(0);
        InventoryBatch batch = new InventoryBatch();
        batch.setBatchCode("B1");
        batch.setAvailableQuantity(1);
        batch.setReservedQuantity(0);
        item.setBatches(List.of(batch));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(true);
        when(inventoryItemRepository.findByProductIdAndWarehouseCode("prod-1", "BLR-1")).thenReturn(Optional.of(item));

        assertThrows(BusinessRuleException.class, () -> inventoryService.reserveStock(
                new ReserveStockRequest("order-1", "prod-1", "SKU-1", "BLR-1", 2)));
    }

    @Test
    void upsertStockShouldAggregateBatchQuantities() {
        when(inventoryItemRepository.findByProductIdAndWarehouseCode("prod-1", "BLR-1")).thenReturn(Optional.empty());
        when(inventoryItemRepository.save(any(InventoryItemDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inventoryService.upsertStock(new StockAdjustmentRequest(
                "prod-1",
                "SKU-1",
                "BLR-1",
                3,
                StockAdjustmentType.INBOUND,
                "Initial stock load",
                List.of(
                        new InventoryBatchRequest("B1", 10, Instant.parse("2026-12-31T00:00:00Z")),
                        new InventoryBatchRequest("B2", 5, Instant.parse("2027-01-31T00:00:00Z")))));

        assertEquals(15, response.availableQuantity());
        assertEquals(0, response.reservedQuantity());
        verify(stockAdjustmentRepository).save(any());
    }
}
