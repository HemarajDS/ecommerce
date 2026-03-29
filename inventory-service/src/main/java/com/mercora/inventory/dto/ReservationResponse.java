package com.mercora.inventory.dto;

import com.mercora.inventory.model.ReservationStatus;
import java.time.Instant;

public record ReservationResponse(
        String reservationCode,
        String orderId,
        String productId,
        String sku,
        String warehouseCode,
        int quantity,
        ReservationStatus status,
        Instant expiresAt) {
}
