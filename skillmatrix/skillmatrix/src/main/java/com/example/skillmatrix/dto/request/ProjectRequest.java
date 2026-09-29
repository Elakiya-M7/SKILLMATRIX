package com.example.skillmatrix.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
		@NotBlank(message = "name must not be blank") String name) {
}