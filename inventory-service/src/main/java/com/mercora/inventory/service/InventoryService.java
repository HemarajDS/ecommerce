package com.mercora.inventory.service;

import com.mercora.inventory.dto.InventoryItemResponse;
import com.mercora.inventory.dto.ReservationResponse;
import com.mercora.inventory.dto.ReserveStockRequest;
import com.mercora.inventory.dto.StockAdjustmentRequest;
import java.util.List;

public interface InventoryService {

    InventoryItemResponse upsertStock(StockAdjustmentRequest request);

    InventoryItemResponse getInventoryByProductAndWarehouse(String productId, String warehouseCode);

    List<InventoryItemResponse> listInventoryByProduct(String productId);

    ReservationResponse reserveStock(ReserveStockRequest request);

    ReservationResponse releaseReservation(String reservationCode);

    ReservationResponse fulfillReservation(String reservationCode);
}
