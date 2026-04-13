package com.cliniq.shared.validation;

/**
 * Lightweight guard utilities for use inside value object canonical constructors
 * and aggregate methods.
 *
 * <p>Throws {@link IllegalArgumentException} on violation — not a domain exception —
 * because these checks guard programming errors (passing null where a value is
 * required), not business rule violations. Business invariant failures throw the
 * appropriate {@code DomainException} subclass directly.
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * Requires that {@code value} is not null.
     *
     * @return the value, for use in compact record constructors
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    /**
     * Requires that {@code value} is not null and not blank.
     *
     * @return the value, for use in compact record constructors
     */
    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    /**
     * Requires that {@code value} is strictly positive (> 0).
     *
     * @return the value, for use in compact record constructors
     */
    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive, got: " + value);
        }
        return value;
    }

    /**
     * Requires that {@code value} is zero or positive (>= 0).
     *
     * @return the value, for use in compact record constructors
     */
    public static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative, got: " + value);
        }
        return value;
    }

    /**
     * Requires that {@code condition} is true.
     * Use when the built-in variants don't cover the check.
     */
    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
