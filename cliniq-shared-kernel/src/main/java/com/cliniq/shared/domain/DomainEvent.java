package com.cliniq.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker contract for all domain events in the system.
 *
 * <p>Intentionally non-sealed so each bounded context can declare its own
 * sealed sub-interface (e.g., {@code AppointmentEvent}, {@code PatientEvent})
 * without requiring changes to this module.
 *
 * <p>Every concrete event must be an immutable record implementing one of
 * those BC-level sealed interfaces.
 */
public interface DomainEvent {

    /** Globally unique identifier for this specific event occurrence. */
    UUID eventId();

    /** Wall-clock time at which the event was raised inside the aggregate. */
    Instant occurredAt();

    /** The tenant in whose context the event was raised. */
    TenantId tenantId();
}
