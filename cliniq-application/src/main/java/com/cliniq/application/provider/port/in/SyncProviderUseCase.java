package com.cliniq.application.provider.port.in;

import com.cliniq.application.provider.command.SyncProviderCommand;

public interface SyncProviderUseCase {
    void sync(SyncProviderCommand command);
}