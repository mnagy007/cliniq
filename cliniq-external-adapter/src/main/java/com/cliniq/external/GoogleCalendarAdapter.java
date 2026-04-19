package com.cliniq.external;


import com.cliniq.application.appointment.port.out.CalendarCredentialRepository;
import com.cliniq.application.appointment.port.out.CalendarSyncPort;
import com.cliniq.domain.appointment.Appointment;
import com.cliniq.shared.domain.TenantId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClient;

public class GoogleCalendarAdapter implements CalendarSyncPort {

    private final RestClient restClient;
    private final CalendarCredentialRepository  calendarCredentialRepository;
    private final ObjectMapper objectMapper;

    public GoogleCalendarAdapter(RestClient restClient,
                                 CalendarCredentialRepository calendarCredentialRepository,
                                 ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.calendarCredentialRepository = calendarCredentialRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String syncAppointment(Appointment appointment) {
        String tokens = calendarCredentialRepository.findDecryptedTokens(appointment.getTenantId()).orElseThrow(() -> {
            throw new IllegalStateException("No calendar found for appointment with id: " + appointment.getId());
        });
        String eventPayload = """
                {
                    "summary": "Appointment",
                    "start": {
                        "dateTime": "%s"
                    },
                    "end": {
                        "dateTime": "%s"
                    }
                }"""
        .formatted(appointment.getTimeSlot().startTime().toString(), appointment.getTimeSlot().endTime().toString());
        String response = restClient.post().uri("/calendar/v3/calendars/primary/events")
                .header("Authorization", "Bearer " + tokens)
                .body(eventPayload).retrieve().body(String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.get("id").toString();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to parse Google Calendar response", exception);
        }
    }

    @Override
    public void deleteEvent(String eventId, TenantId tenantId) {
        String tokens = calendarCredentialRepository.findDecryptedTokens(tenantId).orElseThrow(() -> {
            throw new IllegalStateException("No calendar found for event with id: " + eventId);
        });
        restClient.delete().uri("/calendar/v3/calendars/primary/events/{eventId}", eventId)
                .header("Authorization", "Bearer " + tokens).retrieve();
    }
}
