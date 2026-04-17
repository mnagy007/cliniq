package com.cliniq.web.security;

public class TenantResolutionException extends RuntimeException {

    public TenantResolutionException(String message) {
        super(message);
    }
}
