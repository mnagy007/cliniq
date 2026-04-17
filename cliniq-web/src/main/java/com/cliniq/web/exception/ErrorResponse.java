package com.cliniq.web.exception;

import java.time.Instant;

/**
 * Standard error envelope for all API error responses.
 *
 * <p>Format per REST API contracts:
 * <pre>
 * {
 *   "status": 422,
 *   "error": "VALIDATION_ERROR",
 *   "message": "Human-readable description",
 *   "timestamp": "2026-04-11T10:00:00Z"
 * }
 * </pre>
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now());
    }
}
