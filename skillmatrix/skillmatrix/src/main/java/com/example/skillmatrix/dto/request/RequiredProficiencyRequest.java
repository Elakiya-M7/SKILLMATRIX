package com.example.skillmatrix.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RequiredProficiencyRequest(
		@NotNull(message = "requiredProficiency is required") @Min(value = 1, message = "requiredProficiency must be between 1 and 5") @Max(value = 5, message = "requiredProficiency must be between 1 and 5") Integer requiredProficiency) {
}