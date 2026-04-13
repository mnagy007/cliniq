package com.cliniq.domain.notification.exception;

import com.cliniq.shared.domain.DomainException;

public class NotificationDomainException extends DomainException {
    public NotificationDomainException(String code, String message) {
        super(code, message);
    }
}
