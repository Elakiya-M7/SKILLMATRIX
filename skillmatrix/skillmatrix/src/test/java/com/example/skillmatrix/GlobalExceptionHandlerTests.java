package com.example.skillmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.skillmatrix.exception.ApiErrorResponse;
import com.example.skillmatrix.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

class GlobalExceptionHandlerTests {

	private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

	@Test
	void reportsStringRequiredWhenJsonContainsANumber() {
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
				"Invalid request body",
				new RuntimeException("Cannot deserialize value of type `java.lang.String` from Integer value through reference chain: EmployeeRequest[\"name\"]"),
				null);

		ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnreadableBody(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("name must be a string.", response.getBody().message());
	}

	@Test
	void reportsIntegerRequiredWhenJsonContainsText() {
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
				"Invalid request body",
				new RuntimeException("Cannot deserialize value of type `java.lang.Long` from String \"abc\""),
				null);

		ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnreadableBody(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Request value must be an integer.", response.getBody().message());
	}
}