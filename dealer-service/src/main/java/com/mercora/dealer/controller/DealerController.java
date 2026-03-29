package com.mercora.dealer.controller;

import com.mercora.dealer.dto.ApprovePurchaseOrderRequest;
import com.mercora.dealer.dto.CreatePurchaseOrderRequest;
import com.mercora.dealer.dto.DealerOnboardingRequest;
import com.mercora.dealer.dto.DealerResponse;
import com.mercora.dealer.dto.LedgerEntryResponse;
import com.mercora.dealer.dto.PurchaseOrderResponse;
import com.mercora.dealer.service.DealerService;
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
@RequestMapping("/api/v1")
@Tag(name = "Dealer", description = "Dealer onboarding, purchase order, and ledger APIs")
public class DealerController {

    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @PostMapping("/dealers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Onboard a new dealer")
    public DealerResponse onboardDealer(@Valid @RequestBody DealerOnboardingRequest request) {
        return dealerService.onboardDealer(request);
    }

    @GetMapping("/dealers/{dealerId}")
    @Operation(summary = "Get dealer profile")
    public DealerResponse getDealer(@PathVariable String dealerId) {
        return dealerService.getDealer(dealerId);
    }

    @PostMapping("/dealers/{dealerId}/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a dealer purchase order")
    public PurchaseOrderResponse createPurchaseOrder(@PathVariable String dealerId, @Valid @RequestBody CreatePurchaseOrderRequest request) {
        return dealerService.createPurchaseOrder(dealerId, request);
    }

    @GetMapping("/dealers/{dealerId}/purchase-orders")
    @Operation(summary = "List dealer purchase orders")
    public List<PurchaseOrderResponse> listPurchaseOrders(@PathVariable String dealerId) {
        return dealerService.listPurchaseOrders(dealerId);
    }

    @PostMapping("/dealer-pos/{purchaseOrderId}/approve")
    @Operation(summary = "Approve a pending purchase order")
    public PurchaseOrderResponse approve(@PathVariable String purchaseOrderId, @Valid @RequestBody ApprovePurchaseOrderRequest request) {
        return dealerService.approvePurchaseOrder(purchaseOrderId, request);
    }

    @PostMapping("/dealer-pos/{purchaseOrderId}/reject")
    @Operation(summary = "Reject a pending purchase order")
    public PurchaseOrderResponse reject(@PathVariable String purchaseOrderId, @Valid @RequestBody ApprovePurchaseOrderRequest request) {
        return dealerService.rejectPurchaseOrder(purchaseOrderId, request);
    }

    @GetMapping("/dealers/{dealerId}/ledger")
    @Operation(summary = "Get dealer ledger")
    public List<LedgerEntryResponse> getLedger(@PathVariable String dealerId) {
        return dealerService.getLedger(dealerId);
    }
}
