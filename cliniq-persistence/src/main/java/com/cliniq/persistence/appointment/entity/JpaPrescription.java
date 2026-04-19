package com.cliniq.persistence.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "prescriptions")
public class JpaPrescription {

    @Id
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "ndc_code")
    private String ndcCode;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "generic_name")
    private String genericName;

    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "instructions", nullable = false)
    private String instructions;

    public JpaPrescription() {}

    public JpaPrescription(UUID id, UUID appointmentId, UUID tenantId, String ndcCode,
                    String brandName, String genericName, String dosage, String instructions) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.tenantId = tenantId;
        this.ndcCode = ndcCode;
        this.brandName = brandName;
        this.genericName = genericName;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    public UUID getId() { return id; }
    public UUID getAppointmentId() { return appointmentId; }
    public UUID getTenantId() { return tenantId; }
    public String getNdcCode() { return ndcCode; }
    public String getBrandName() { return brandName; }
    public String getGenericName() { return genericName; }
    public String getDosage() { return dosage; }
    public String getInstructions() { return instructions; }
}
