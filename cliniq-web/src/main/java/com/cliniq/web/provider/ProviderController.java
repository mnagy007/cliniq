package com.cliniq.web.provider;

import com.cliniq.application.provider.command.SyncProviderCommand;
import com.cliniq.application.provider.port.in.QueryProviderUseCase;
import com.cliniq.application.provider.port.in.SyncProviderUseCase;
import com.cliniq.domain.provider.AvailabilitySlot;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.domain.provider.ProviderName;
import com.cliniq.domain.provider.Specialty;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.provider.dto.AvailabilitySlotDto;
import com.cliniq.web.provider.dto.ProviderResponse;
import com.cliniq.web.provider.dto.SyncProviderRequest;
import com.cliniq.web.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final SyncProviderUseCase syncProviderUseCase;
    private final QueryProviderUseCase queryProviderUseCase;

    public ProviderController(SyncProviderUseCase syncProviderUseCase,
                              QueryProviderUseCase queryProviderUseCase) {
        this.syncProviderUseCase = syncProviderUseCase;
        this.queryProviderUseCase = queryProviderUseCase;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync(@Valid @RequestBody SyncProviderRequest request) {
        TenantId tenantId = TenantContext.require();
        ProviderId providerId = ProviderId.generate();

        List<AvailabilitySlot> slots = mapSlots(request.availabilitySlots());
        ProviderName name = new ProviderName(request.givenName(), request.familyName());
        Specialty specialty = Specialty.valueOf(request.specialty());

        SyncProviderCommand command = new SyncProviderCommand(
                tenantId, providerId, name, specialty, slots);

        syncProviderUseCase.sync(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProvider(@PathVariable UUID id) {
        TenantId tenantId = TenantContext.require();
        Provider provider = queryProviderUseCase.findById(tenantId, ProviderId.of(id));
        return ResponseEntity.ok(toProviderResponse(provider));
    }

    private static List<AvailabilitySlot> mapSlots(List<AvailabilitySlotDto> slotDtos) {
        if (slotDtos == null) {
            return List.of();
        }
        List<AvailabilitySlot> slots = new ArrayList<>();
        for (AvailabilitySlotDto dto : slotDtos) {
            slots.add(new AvailabilitySlot(
                    DayOfWeek.valueOf(dto.dayOfWeek()),
                    dto.startTime(),
                    dto.endTime()));
        }
        return slots;
    }

    private static ProviderResponse toProviderResponse(Provider provider) {
        List<AvailabilitySlotDto> slotDtos = new ArrayList<>();
        for (AvailabilitySlot slot : provider.getAvailabilitySlots()) {
            slotDtos.add(new AvailabilitySlotDto(
                    slot.dayOfWeek().name(),
                    slot.startTime(),
                    slot.endTime()));
        }

        return new ProviderResponse(
                provider.getId().value(),
                provider.getTenantId().value(),
                provider.getName().givenName(),
                provider.getName().familyName(),
                provider.getSpecialty().name(),
                slotDtos);
    }
}