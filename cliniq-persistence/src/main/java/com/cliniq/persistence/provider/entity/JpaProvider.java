package com.cliniq.persistence.provider.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name="providers")
@Filter(name="tenantFilter", condition="tenant_id = :tenantId")
public class JpaProvider {

   @Id
   private UUID id;

   @Column(name="tenant_id", nullable=false)
   private UUID tenantId;

   @Column(name="given_name", nullable=false)
   private String givenName ;

   @Column(name="family_name", nullable=false)
   private String familyName;

   @Column(name="specialty", nullable=false)
   private String specialty;

   @Column(name="created_at", nullable=false, updatable=false)
   private OffsetDateTime createdAt;

   @Column(name="updated_at", nullable=false)
   private OffsetDateTime updatedAt;

   @Column(name="version", nullable=false)
   @Version
   private Long version;

    @OneToMany(cascade=ALL, orphanRemoval=true, fetch=EAGER)
    @JoinColumn(name = "provider_id")
    private List<JpaAvailabilitySlot> slots = new ArrayList<>();

   public JpaProvider() {}

   public JpaProvider(UUID id, UUID tenantId, String givenName, String familyName,
               String specialty, List<JpaAvailabilitySlot> slots) {
       this.id = id;
       this.tenantId = tenantId;
       this.givenName = givenName;
       this.familyName = familyName;
       this.specialty = specialty;
       this.slots = slots != null ? slots : new ArrayList<>();
   }

   public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<JpaAvailabilitySlot> getSlots() {
        return slots;
    }

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
