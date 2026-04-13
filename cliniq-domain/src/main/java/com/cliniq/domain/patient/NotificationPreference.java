package com.cliniq.domain.patient;

import com.cliniq.domain.notification.Channel;
import com.cliniq.shared.domain.ValueObject;
import com.cliniq.shared.validation.Preconditions;

public record NotificationPreference(Channel preferredChannel, boolean optedOut) implements ValueObject {

    public NotificationPreference {
        Preconditions.requireNonNull(preferredChannel, "preferredChannel");
    }

    public static NotificationPreference defaultPreference() {
        return new NotificationPreference(Channel.Sms.INSTANCE, false);
    }
}