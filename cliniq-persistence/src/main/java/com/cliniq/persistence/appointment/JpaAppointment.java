package com.cliniq.persistence.appointment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class JpaAppointment {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_start", nullable = false)
    private LocalTime slotStart;

    @Column(name = "slot_end", nullable = false)
    private LocalTime slotEnd;

    @Column(name = "type", nullable = false)
    private String appointmentType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "cancellation_code")
    private String cancellationCode;

    @Column(name = "cancellation_description")
    private String cancellationDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    JpaAppointment() {}

    JpaAppointment(UUID id, UUID tenantId, UUID patientId, UUID providerId,
                   LocalDate slotDate, LocalTime slotStart, LocalTime slotEnd,
                   String appointmentType, String status,
                   String cancellationCode, String cancellationDescription) {
        this.id = id;
        this.tenantId = tenantId;
        this.patientId = patientId;
        this.providerId = providerId;
        this.slotDate = slotDate;
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
        this.appointmentType = appointmentType;
        this.status = status;
        this.cancellationCode = cancellationCode;
        this.cancellationDescription = cancellationDescription;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getPatientId() { return patientId; }
    public UUID getProviderId() { return providerId; }
    public LocalDate getSlotDate() { return slotDate; }
    public LocalTime getSlotStart() { return slotStart; }
    public LocalTime getSlotEnd() { return slotEnd; }
    public String getAppointmentType() { return appointmentType; }
    public String getStatus() { return status; }
    public String getCancellationCode() { return cancellationCode; }
    public String getCancellationDescription() { return cancellationDescription; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}