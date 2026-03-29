package com.mercora.inventory.controller;

import com.mercora.inventory.dto.InventoryItemResponse;
import com.mercora.inventory.dto.ReservationResponse;
import com.mercora.inventory.dto.ReserveStockRequest;
import com.mercora.inventory.dto.StockAdjustmentRequest;
import com.mercora.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Inventory, reservations, and stock operations")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create or replace stock for a warehouse SKU")
    public InventoryItemResponse upsertStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryService.upsertStock(request);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List inventory records for a product")
    public List<InventoryItemResponse> listByProduct(@PathVariable String productId) {
        return inventoryService.listInventoryByProduct(productId);
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseCode}")
    @Operation(summary = "Get inventory for a product in a warehouse")
    public InventoryItemResponse getByWarehouse(@PathVariable String productId, @PathVariable String warehouseCode) {
        return inventoryService.getInventoryByProductAndWarehouse(productId, warehouseCode);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve stock for an order")
    public ReservationResponse reserve(@Valid @RequestBody ReserveStockRequest request) {
        return inventoryService.reserveStock(request);
    }

    @PostMapping("/reservations/{reservationCode}/release")
    @Operation(summary = "Release a stock reservation")
    public ReservationResponse release(@PathVariable String reservationCode) {
        return inventoryService.releaseReservation(reservationCode);
    }

    @PostMapping("/reservations/{reservationCode}/fulfill")
    @Operation(summary = "Fulfill a reservation after successful allocation")
    public ReservationResponse fulfill(@PathVariable String reservationCode) {
        return inventoryService.fulfillReservation(reservationCode);
    }
}
