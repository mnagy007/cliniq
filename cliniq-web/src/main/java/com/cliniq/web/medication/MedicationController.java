package com.cliniq.web.medication;

import com.cliniq.application.medication.port.out.MedicationLookupPort;
import com.cliniq.domain.appointment.MedicationReference;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.medication.dto.MedicationResponse;
import com.cliniq.web.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final MedicationLookupPort medicationLookupPort;

    public MedicationController(MedicationLookupPort medicationLookupPort) {
        this.medicationLookupPort = medicationLookupPort;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<MedicationResponse>>> search(@RequestParam String q) {
        TenantId tenantId = TenantContext.require();
        List<MedicationReference> results = medicationLookupPort.search(q, tenantId);

        List<MedicationResponse> response = results.stream()
                .map(ref -> new MedicationResponse(ref.ndcCode(), ref.brandName(), ref.genericName()))
                .toList();

        return ResponseEntity.ok(Map.of("results", response));
    }
}