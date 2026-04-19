package com.cliniq.persistence.appointment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="calendar_credentials")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class JpaCalendarCredential {

    @Id
    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;
    @Column(name = "encrypted_tokens")
    private String encryptedTokens;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public JpaCalendarCredential() {
    }

    public JpaCalendarCredential(UUID tenantId, String encryptedTokens) {
        this.tenantId = tenantId;
        this.encryptedTokens = encryptedTokens;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getEncryptedTokens() {
        return encryptedTokens;
    }

    public void setEncryptedTokens(String encryptedTokens) {
        this.encryptedTokens = encryptedTokens;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
