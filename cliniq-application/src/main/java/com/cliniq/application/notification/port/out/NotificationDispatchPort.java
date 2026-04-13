package com.cliniq.application.notification.port.out;

import com.cliniq.domain.notification.Reminder;
import com.cliniq.domain.patient.ContactInfo;

public interface NotificationDispatchPort {
    DispatchResult dispatch(Reminder reminder, ContactInfo contactInfo);
}