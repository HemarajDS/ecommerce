package com.mercora.dealer.service;

import com.mercora.dealer.dto.ApprovePurchaseOrderRequest;
import com.mercora.dealer.dto.CreatePurchaseOrderRequest;
import com.mercora.dealer.dto.DealerOnboardingRequest;
import com.mercora.dealer.dto.DealerResponse;
import com.mercora.dealer.dto.LedgerEntryResponse;
import com.mercora.dealer.dto.PurchaseOrderResponse;
import java.util.List;

public interface DealerService {

    DealerResponse onboardDealer(DealerOnboardingRequest request);

    DealerResponse getDealer(String dealerId);

    PurchaseOrderResponse createPurchaseOrder(String dealerId, CreatePurchaseOrderRequest request);

    PurchaseOrderResponse approvePurchaseOrder(String purchaseOrderId, ApprovePurchaseOrderRequest request);

    PurchaseOrderResponse rejectPurchaseOrder(String purchaseOrderId, ApprovePurchaseOrderRequest request);

    List<PurchaseOrderResponse> listPurchaseOrders(String dealerId);

    List<LedgerEntryResponse> getLedger(String dealerId);
}
