package com.cliniq.application.medication.port.out;

import com.cliniq.domain.appointment.MedicationReference;
import com.cliniq.shared.domain.TenantId;

import java.util.List;

public interface MedicationLookupPort {
    List<MedicationReference> search(String query, TenantId tenantId);
}