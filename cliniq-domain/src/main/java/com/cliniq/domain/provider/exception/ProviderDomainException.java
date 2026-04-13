package com.cliniq.domain.provider.exception;

import com.cliniq.shared.domain.DomainException;

public class ProviderDomainException extends DomainException {

    public ProviderDomainException(String errorCode, String message) {
        super(errorCode, message);
    }
}