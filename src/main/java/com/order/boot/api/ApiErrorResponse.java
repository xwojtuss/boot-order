package com.order.boot.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ApiErrorResponse", description = "Error returned by the API")
public record ApiErrorResponse(
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        @Schema(example = "Validation failed")
        String message,
        @Schema(example = "{\"username\":\"Username is required\"}")
        Map<String, String> fieldErrors
) {
}
