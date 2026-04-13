package com.cliniq.application.notification.port.out;

public record DispatchResult(boolean success, String externalId, String errorMessage) {
    public static DispatchResult success(String externalId) {
        return new DispatchResult(true, externalId, null);
    }

    public static DispatchResult failure(String errorMessage) {
        return new DispatchResult(false, null, errorMessage);
    }
}