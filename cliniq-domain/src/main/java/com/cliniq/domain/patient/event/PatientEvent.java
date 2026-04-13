package com.cliniq.domain.patient.event;

import com.cliniq.shared.domain.DomainEvent;
import com.cliniq.shared.domain.TenantId;

public sealed interface PatientEvent extends DomainEvent
        permits PatientRegistered, ContactInfoUpdated, NotificationPreferenceChanged {
}