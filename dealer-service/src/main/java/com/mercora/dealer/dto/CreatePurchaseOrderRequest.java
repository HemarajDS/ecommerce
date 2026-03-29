package com.mercora.dealer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreatePurchaseOrderRequest(@Valid @NotEmpty List<PurchaseOrderItemRequest> items) {
}
