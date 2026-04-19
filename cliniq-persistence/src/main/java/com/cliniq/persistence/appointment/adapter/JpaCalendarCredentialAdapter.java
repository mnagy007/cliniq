package com.cliniq.persistence.appointment.adapter;

import com.cliniq.application.appointment.port.out.CalendarCredentialRepository;
import com.cliniq.application.shared.port.out.EncryptionPort;
import com.cliniq.persistence.appointment.entity.JpaCalendarCredential;
import com.cliniq.persistence.appointment.repository.JpaCalendarCredentialRepository;
import com.cliniq.shared.domain.TenantId;

import java.util.Optional;

public class JpaCalendarCredentialAdapter implements CalendarCredentialRepository {

    private final JpaCalendarCredentialRepository repository;
    private final EncryptionPort encryptionPort;

    public JpaCalendarCredentialAdapter(JpaCalendarCredentialRepository repository, EncryptionPort encryptionPort) {
        this.repository = repository;
        this.encryptionPort = encryptionPort;
    }

    @Override
    public Optional<String> findDecryptedTokens(TenantId tenantId) {
        return repository.findById(tenantId.value())
                .map(entity -> encryptionPort.decrypt(entity.getEncryptedTokens()));
    }

    @Override
    public void save(TenantId tenantId, String plainTextTokens) {
        String encrypted = encryptionPort.encrypt(plainTextTokens);
        JpaCalendarCredential entity = new JpaCalendarCredential(tenantId.value(), encrypted);
        repository.save(entity);
    }
}
