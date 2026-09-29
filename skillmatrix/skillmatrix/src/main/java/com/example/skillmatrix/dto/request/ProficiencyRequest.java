package com.example.skillmatrix.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProficiencyRequest(
		@NotNull(message = "proficiency is required") @Min(value = 1, message = "proficiency must be between 1 and 5") @Max(value = 5, message = "proficiency must be between 1 and 5") Integer proficiency) {
}