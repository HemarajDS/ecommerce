package com.mercora.dealer.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovePurchaseOrderRequest(@NotBlank String reason) {
}
