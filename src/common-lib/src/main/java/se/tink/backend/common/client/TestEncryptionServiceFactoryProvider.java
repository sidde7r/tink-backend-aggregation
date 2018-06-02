package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import se.tink.backend.encryption.client.ClientEncryptionServiceFactory;
import se.tink.backend.encryption.client.EncryptionServiceFactory;

public class TestEncryptionServiceFactoryProvider implements Provider<EncryptionServiceFactory> {

    private String wireMockUrl;

    @Inject
    public TestEncryptionServiceFactoryProvider(@Named("wireMockUrl") String wireMockUrl) {
        this.wireMockUrl = wireMockUrl;
    }

    @Override
    public EncryptionServiceFactory get() {
        return ClientEncryptionServiceFactory.buildWithoutPinning(wireMockUrl);
    }
}
