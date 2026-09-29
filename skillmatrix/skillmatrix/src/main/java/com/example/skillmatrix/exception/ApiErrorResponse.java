package com.example.skillmatrix.exception;

import java.time.Instant;

public record ApiErrorResponse(Instant timestamp, int status, String message) {
}