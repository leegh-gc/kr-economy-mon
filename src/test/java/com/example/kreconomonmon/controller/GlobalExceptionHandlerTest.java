package com.example.kreconomonmon.controller;

import com.example.kreconomonmon.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returns_400() {
        ResponseEntity<ApiErrorResponse> response =
            handler.handleIllegalArgument(new IllegalArgumentException("잘못된 값"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo("잘못된 값");
    }

    @Test
    void handleRuntimeException_returns_500() {
        ResponseEntity<ApiErrorResponse> response =
            handler.handleRuntimeException(new RuntimeException("서버 에러"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void handleException_returns_500() {
        ResponseEntity<ApiErrorResponse> response =
            handler.handleException(new Exception("예외"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_ERROR");
    }
}
