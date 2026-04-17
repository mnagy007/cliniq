package com.cliniq.web.provider;

import com.cliniq.application.provider.port.in.QueryProviderUseCase;
import com.cliniq.application.provider.port.in.SyncProviderUseCase;
import com.cliniq.domain.provider.AvailabilitySlot;
import com.cliniq.domain.provider.Provider;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.domain.provider.ProviderName;
import com.cliniq.domain.provider.Specialty;
import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.provider.dto.AvailabilitySlotDto;
import com.cliniq.web.provider.dto.SyncProviderRequest;
import com.cliniq.web.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderControllerTest {

    private static final UUID TENANT_UUID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PROVIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final TenantId TENANT    = TenantId.of(TENANT_UUID);

    @Mock private SyncProviderUseCase  syncProviderUseCase;
    @Mock private QueryProviderUseCase queryProviderUseCase;

    private ProviderController controller;

    @BeforeEach
    void setUp() {
        controller = new ProviderController(syncProviderUseCase, queryProviderUseCase);
    }

    @Test
    @DisplayName("sync returns 200 OK and passes correctly mapped command")
    void sync_validRequest_returnsOk() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var slot = new AvailabilitySlotDto("MONDAY", LocalTime.of(9, 0), LocalTime.of(17, 0));
            var request = new SyncProviderRequest("Sara", "Khalil", "CARDIOLOGY", List.of(slot));

            var response = controller.sync(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(syncProviderUseCase).sync(argThat(cmd ->
                    cmd.tenantId().equals(TENANT) &&
                    cmd.name().givenName().equals("Sara") &&
                    cmd.name().familyName().equals("Khalil") &&
                    cmd.specialty() == Specialty.CARDIOLOGY &&
                    cmd.availabilitySlots().size() == 1 &&
                    cmd.availabilitySlots().get(0).dayOfWeek() == DayOfWeek.MONDAY));
        }
    }

    @Test
    @DisplayName("sync with null slots treats as empty list")
    void sync_nullSlots_mapsToEmptyList() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            var request = new SyncProviderRequest("Sara", "Khalil", "GENERAL_PRACTICE", null);
            controller.sync(request);

            verify(syncProviderUseCase).sync(argThat(cmd ->
                    cmd.availabilitySlots().isEmpty()));
        }
    }

    @Test
    @DisplayName("getProvider returns 200 with mapped provider response")
    void getProvider_existingProvider_returnsOk() {
        try (var ctx = mockStatic(TenantContext.class)) {
            ctx.when(TenantContext::require).thenReturn(TENANT);

            Provider provider = mockProvider();
            when(queryProviderUseCase.findById(TENANT, ProviderId.of(PROVIDER_UUID)))
                    .thenReturn(provider);

            var response = controller.getProvider(PROVIDER_UUID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().id()).isEqualTo(PROVIDER_UUID);
            assertThat(response.getBody().givenName()).isEqualTo("Sara");
            assertThat(response.getBody().specialty()).isEqualTo("CARDIOLOGY");
            assertThat(response.getBody().availabilitySlots()).hasSize(1);
            assertThat(response.getBody().availabilitySlots().get(0).dayOfWeek()).isEqualTo("MONDAY");
        }
    }

    private Provider mockProvider() {
        return Provider.reconstruct(
                ProviderId.of(PROVIDER_UUID), TENANT,
                new ProviderName("Sara", "Khalil"),
                Specialty.CARDIOLOGY,
                List.of(new AvailabilitySlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))));
    }
}
