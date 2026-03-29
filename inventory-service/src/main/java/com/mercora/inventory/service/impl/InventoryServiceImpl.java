package com.mercora.inventory.service.impl;

import com.mercora.inventory.config.InventoryProperties;
import com.mercora.inventory.dto.InventoryBatchRequest;
import com.mercora.inventory.dto.InventoryBatchResponse;
import com.mercora.inventory.dto.InventoryItemResponse;
import com.mercora.inventory.dto.ReservationResponse;
import com.mercora.inventory.dto.ReserveStockRequest;
import com.mercora.inventory.dto.StockAdjustmentRequest;
import com.mercora.inventory.event.LowStockEventPublisher;
import com.mercora.inventory.exception.BusinessRuleException;
import com.mercora.inventory.exception.ResourceNotFoundException;
import com.mercora.inventory.model.InventoryBatch;
import com.mercora.inventory.model.InventoryItemDocument;
import com.mercora.inventory.model.InventoryReservationDocument;
import com.mercora.inventory.model.ReservationStatus;
import com.mercora.inventory.model.StockAdjustmentDocument;
import com.mercora.inventory.repository.InventoryItemRepository;
import com.mercora.inventory.repository.InventoryReservationRepository;
import com.mercora.inventory.repository.StockAdjustmentRepository;
import com.mercora.inventory.service.InventoryService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StringRedisTemplate redisTemplate;
    private final InventoryProperties inventoryProperties;
    private final LowStockEventPublisher lowStockEventPublisher;

    public InventoryServiceImpl(
            InventoryItemRepository inventoryItemRepository,
            InventoryReservationRepository inventoryReservationRepository,
            StockAdjustmentRepository stockAdjustmentRepository,
            StringRedisTemplate redisTemplate,
            InventoryProperties inventoryProperties,
            LowStockEventPublisher lowStockEventPublisher) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.redisTemplate = redisTemplate;
        this.inventoryProperties = inventoryProperties;
        this.lowStockEventPublisher = lowStockEventPublisher;
    }

    @Override
    public InventoryItemResponse upsertStock(StockAdjustmentRequest request) {
        InventoryItemDocument item = inventoryItemRepository.findByProductIdAndWarehouseCode(request.productId(), request.warehouseCode())
                .orElseGet(InventoryItemDocument::new);

        item.setProductId(request.productId());
        item.setSku(request.sku());
        item.setWarehouseCode(request.warehouseCode());
        item.setSafetyStock(request.safetyStock() == null ? inventoryProperties.getDefaultSafetyStock() : request.safetyStock());
        item.setBatches(request.batches().stream().map(this::toBatch).toList());
        item.setAvailableQuantity(item.getBatches().stream().mapToInt(InventoryBatch::getAvailableQuantity).sum());
        item.setReservedQuantity(item.getBatches().stream().mapToInt(InventoryBatch::getReservedQuantity).sum());
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(Instant.now());
        }
        item.setUpdatedAt(Instant.now());

        InventoryItemDocument saved = inventoryItemRepository.save(item);

        StockAdjustmentDocument adjustment = new StockAdjustmentDocument();
        adjustment.setInventoryItemId(saved.getId());
        adjustment.setProductId(saved.getProductId());
        adjustment.setSku(saved.getSku());
        adjustment.setWarehouseCode(saved.getWarehouseCode());
        adjustment.setType(request.type());
        adjustment.setQuantity(saved.getAvailableQuantity());
        adjustment.setReason(request.reason());
        adjustment.setCreatedAt(Instant.now());
        stockAdjustmentRepository.save(adjustment);

        lowStockEventPublisher.publishIfNeeded(saved);
        return toItemResponse(saved);
    }

    @Override
    public InventoryItemResponse getInventoryByProductAndWarehouse(String productId, String warehouseCode) {
        return inventoryItemRepository.findByProductIdAndWarehouseCode(productId, warehouseCode)
                .map(this::toItemResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }

    @Override
    public List<InventoryItemResponse> listInventoryByProduct(String productId) {
        return inventoryItemRepository.findByProductId(productId).stream().map(this::toItemResponse).toList();
    }

    @Override
    public ReservationResponse reserveStock(ReserveStockRequest request) {
        String lockKey = "inventory:lock:" + request.productId() + ":" + request.warehouseCode();
        boolean locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                lockKey, "1", Duration.ofSeconds(inventoryProperties.getLockTtlSeconds())));
        if (!locked) {
            throw new BusinessRuleException("Inventory is busy, please retry");
        }

        try {
            InventoryItemDocument item = inventoryItemRepository.findByProductIdAndWarehouseCode(request.productId(), request.warehouseCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

            if (item.getAvailableQuantity() < request.quantity()) {
                throw new BusinessRuleException("Insufficient stock available");
            }

            reserveAcrossBatches(item, request.quantity());
            item.setAvailableQuantity(item.getAvailableQuantity() - request.quantity());
            item.setReservedQuantity(item.getReservedQuantity() + request.quantity());
            item.setUpdatedAt(Instant.now());
            InventoryItemDocument saved = inventoryItemRepository.save(item);

            InventoryReservationDocument reservation = new InventoryReservationDocument();
            reservation.setReservationCode(UUID.randomUUID().toString());
            reservation.setOrderId(request.orderId());
            reservation.setProductId(request.productId());
            reservation.setSku(request.sku());
            reservation.setWarehouseCode(request.warehouseCode());
            reservation.setQuantity(request.quantity());
            reservation.setStatus(ReservationStatus.RESERVED);
            reservation.setExpiresAt(Instant.now().plusSeconds(inventoryProperties.getReservationTtlMinutes() * 60));
            reservation.setCreatedAt(Instant.now());
            reservation.setUpdatedAt(Instant.now());
            InventoryReservationDocument savedReservation = inventoryReservationRepository.save(reservation);

            lowStockEventPublisher.publishIfNeeded(saved);
            return toReservationResponse(savedReservation);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public ReservationResponse releaseReservation(String reservationCode) {
        InventoryReservationDocument reservation = inventoryReservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessRuleException("Only active reservations can be released");
        }

        InventoryItemDocument item = inventoryItemRepository.findByProductIdAndWarehouseCode(reservation.getProductId(), reservation.getWarehouseCode())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        releaseAcrossBatches(item, reservation.getQuantity());
        item.setAvailableQuantity(item.getAvailableQuantity() + reservation.getQuantity());
        item.setReservedQuantity(item.getReservedQuantity() - reservation.getQuantity());
        item.setUpdatedAt(Instant.now());
        inventoryItemRepository.save(item);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setUpdatedAt(Instant.now());
        return toReservationResponse(inventoryReservationRepository.save(reservation));
    }

    @Override
    public ReservationResponse fulfillReservation(String reservationCode) {
        InventoryReservationDocument reservation = inventoryReservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessRuleException("Only active reservations can be fulfilled");
        }

        InventoryItemDocument item = inventoryItemRepository.findByProductIdAndWarehouseCode(reservation.getProductId(), reservation.getWarehouseCode())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        item.setReservedQuantity(item.getReservedQuantity() - reservation.getQuantity());
        item.setUpdatedAt(Instant.now());
        inventoryItemRepository.save(item);

        StockAdjustmentDocument adjustment = new StockAdjustmentDocument();
        adjustment.setInventoryItemId(item.getId());
        adjustment.setProductId(item.getProductId());
        adjustment.setSku(item.getSku());
        adjustment.setWarehouseCode(item.getWarehouseCode());
        adjustment.setType(com.mercora.inventory.model.StockAdjustmentType.RESERVATION_FULFILLMENT);
        adjustment.setQuantity(reservation.getQuantity());
        adjustment.setReason("Reservation fulfilled");
        adjustment.setCreatedAt(Instant.now());
        stockAdjustmentRepository.save(adjustment);

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservation.setUpdatedAt(Instant.now());
        return toReservationResponse(inventoryReservationRepository.save(reservation));
    }

    private InventoryBatch toBatch(InventoryBatchRequest batchRequest) {
        InventoryBatch batch = new InventoryBatch();
        batch.setBatchCode(batchRequest.batchCode());
        batch.setAvailableQuantity(batchRequest.quantity());
        batch.setReservedQuantity(0);
        batch.setExpiresAt(batchRequest.expiresAt());
        return batch;
    }

    private void reserveAcrossBatches(InventoryItemDocument item, int quantity) {
        int remaining = quantity;
        for (InventoryBatch batch : item.getBatches()) {
            if (remaining == 0) {
                break;
            }
            int allocatable = Math.min(batch.getAvailableQuantity(), remaining);
            batch.setAvailableQuantity(batch.getAvailableQuantity() - allocatable);
            batch.setReservedQuantity(batch.getReservedQuantity() + allocatable);
            remaining -= allocatable;
        }
        if (remaining > 0) {
            throw new BusinessRuleException("Inventory batches are inconsistent with available stock");
        }
    }

    private void releaseAcrossBatches(InventoryItemDocument item, int quantity) {
        int remaining = quantity;
        for (InventoryBatch batch : item.getBatches()) {
            if (remaining == 0) {
                break;
            }
            int releasable = Math.min(batch.getReservedQuantity(), remaining);
            batch.setReservedQuantity(batch.getReservedQuantity() - releasable);
            batch.setAvailableQuantity(batch.getAvailableQuantity() + releasable);
            remaining -= releasable;
        }
        if (remaining > 0) {
            throw new BusinessRuleException("Reserved stock is inconsistent across batches");
        }
    }

    private InventoryItemResponse toItemResponse(InventoryItemDocument item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getWarehouseCode(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getSafetyStock(),
                item.getBatches() == null ? List.of() : item.getBatches().stream()
                        .map(batch -> new InventoryBatchResponse(
                                batch.getBatchCode(),
                                batch.getAvailableQuantity(),
                                batch.getReservedQuantity(),
                                batch.getExpiresAt()))
                        .toList(),
                item.getUpdatedAt());
    }

    private ReservationResponse toReservationResponse(InventoryReservationDocument reservation) {
        return new ReservationResponse(
                reservation.getReservationCode(),
                reservation.getOrderId(),
                reservation.getProductId(),
                reservation.getSku(),
                reservation.getWarehouseCode(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getExpiresAt());
    }
}
