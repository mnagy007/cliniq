package com.cliniq.shared.domain;

/**
 * Root of the domain exception hierarchy.
 *
 * <p>Every bounded context defines its own subclass (e.g.,
 * {@code AppointmentDomainException}, {@code PatientDomainException}).
 * Leaf exceptions (e.g., {@code AppointmentNotFoundException}) extend those
 * BC-specific intermediates, never this class directly.
 *
 * <p>The {@code errorCode} is a machine-readable string (e.g.,
 * {@code "APPOINTMENT_NOT_FOUND"}) used by the web adapter's global
 * exception handler to map domain failures to HTTP problem details without
 * an instanceof chain.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
