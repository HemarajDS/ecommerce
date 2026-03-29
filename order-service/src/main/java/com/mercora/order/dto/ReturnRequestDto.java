package com.mercora.order.dto;

import jakarta.validation.constraints.NotBlank;

public record ReturnRequestDto(@NotBlank String reason) {
}
