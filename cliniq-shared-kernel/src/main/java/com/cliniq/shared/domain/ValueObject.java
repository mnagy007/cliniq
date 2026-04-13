package com.cliniq.shared.domain;

/**
 * Marker interface for value objects.
 *
 * <p>Value objects must be immutable. In practice, prefer Java {@code record}
 * types for value objects — they provide structural equality, immutability,
 * and a canonical constructor for free. Implement this interface on records
 * or final classes where you want the domain model to be explicitly self-documenting.
 */
public interface ValueObject {
}
