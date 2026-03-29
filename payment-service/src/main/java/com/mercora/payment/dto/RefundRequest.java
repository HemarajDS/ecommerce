package com.mercora.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(@NotBlank String reason) {
}
