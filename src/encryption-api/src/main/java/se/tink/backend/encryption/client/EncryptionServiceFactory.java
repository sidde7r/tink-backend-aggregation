package se.tink.backend.encryption.client;

import se.tink.backend.encryption.api.EncryptionService;

public interface EncryptionServiceFactory {
    public static final String SERVICE_NAME = "encryption";

    EncryptionService getEncryptionService();
}
