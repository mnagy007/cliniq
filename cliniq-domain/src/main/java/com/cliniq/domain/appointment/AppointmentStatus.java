package com.cliniq.domain.appointment;

public sealed interface AppointmentStatus
        permits AppointmentStatus.Scheduled, AppointmentStatus.Confirmed,
                AppointmentStatus.Cancelled, AppointmentStatus.Completed,
                AppointmentStatus.NoShow {

    record Scheduled() implements AppointmentStatus {}

    record Confirmed() implements AppointmentStatus {}

    record Cancelled(CancellationReason reason) implements AppointmentStatus {}

    record Completed() implements AppointmentStatus {}

    record NoShow() implements AppointmentStatus {}
}