package com.example.skillmatrix.exception;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Pattern FIELD_PATTERN = Pattern.compile("\\[\\\"([^\\\"]+)\\\"\\]");

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(CapacityExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleCapacityExceeded(CapacityExceededException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		return error(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
		String details = exception.getMostSpecificCause().getMessage();
		String field = findFieldName(details);
		String normalizedDetails = details == null ? "" : details.toLowerCase(Locale.ROOT);

		if (normalizedDetails.contains("java.lang.string") || normalizedDetails.contains("enum")) {
			return error(HttpStatus.BAD_REQUEST, fieldMessage(field, "must be a string"));
		}
		if (normalizedDetails.contains("java.lang.integer") || normalizedDetails.contains("java.lang.long")) {
			return error(HttpStatus.BAD_REQUEST, fieldMessage(field, "must be an integer"));
		}
		if (normalizedDetails.contains("java.lang.boolean")) {
			return error(HttpStatus.BAD_REQUEST, fieldMessage(field, "must be true or false"));
		}
		return error(HttpStatus.BAD_REQUEST, "Request body contains invalid JSON or a value with the wrong type.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		String expectedType = exception.getRequiredType() == null ? "valid" : exception.getRequiredType().getSimpleName();
		if (expectedType.equals("Long") || expectedType.equals("Integer")) {
			return error(HttpStatus.BAD_REQUEST, exception.getName() + " must be an integer.");
		}
		return error(HttpStatus.BAD_REQUEST, exception.getName() + " must be a " + expectedType.toLowerCase(Locale.ROOT) + ".");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataConflict(DataIntegrityViolationException exception) {
		return error(HttpStatus.CONFLICT, "A resource with the same unique value already exists.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
	}

	private String findFieldName(String details) {
		if (details == null) {
			return null;
		}
		Matcher matcher = FIELD_PATTERN.matcher(details);
		String field = null;
		while (matcher.find()) {
			field = matcher.group(1);
		}
		return field;
	}

	private String fieldMessage(String field, String message) {
		return field == null ? "Request value " + message + "." : field + " " + message + ".";
	}

	private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(Instant.now(), status.value(), message));
	}
}