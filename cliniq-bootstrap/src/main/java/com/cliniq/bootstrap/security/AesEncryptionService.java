package com.cliniq.bootstrap.security;

import com.cliniq.application.shared.port.out.EncryptionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesEncryptionService implements EncryptionPort {

    @Value("${cliniq.encryption.key}")
    private String encryptionKey;

    @Override
    public String encrypt(String plaintext) {
        return "encrypted(" + plaintext + ")";
    }

    @Override
    public String decrypt(String ciphertext) {
        return "decrypted(" + ciphertext + ")";
    }
}
