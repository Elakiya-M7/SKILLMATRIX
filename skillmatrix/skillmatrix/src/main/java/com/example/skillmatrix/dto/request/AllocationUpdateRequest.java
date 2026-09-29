package com.example.skillmatrix.dto.request;

import com.example.skillmatrix.entity.AllocationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AllocationUpdateRequest(
		@NotNull(message = "allocationPercentage is required") @Min(value = 1, message = "allocationPercentage must be between 1 and 100") @Max(value = 100, message = "allocationPercentage must be between 1 and 100") Integer allocationPercentage,
		@NotNull(message = "allocationType is required") AllocationType allocationType) {
}