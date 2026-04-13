package com.cliniq.domain.appointment.event;

import com.cliniq.domain.appointment.AppointmentId;
import com.cliniq.domain.appointment.AppointmentType;
import com.cliniq.domain.appointment.TimeSlot;
import com.cliniq.domain.patient.PatientId;
import com.cliniq.domain.provider.ProviderId;
import com.cliniq.shared.domain.DomainEvent;
import com.cliniq.shared.domain.TenantId;

import java.time.Instant;
import java.util.UUID;

public sealed interface AppointmentEvent extends DomainEvent
        permits AppointmentScheduled, AppointmentConfirmed, AppointmentCancelled,
                AppointmentCompleted, AppointmentMarkedNoShow {
}