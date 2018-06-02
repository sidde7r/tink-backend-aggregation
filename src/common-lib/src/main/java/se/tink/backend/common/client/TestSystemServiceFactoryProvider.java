package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import se.tink.backend.system.client.ClientSystemServiceFactory;
import se.tink.backend.system.client.SystemServiceFactory;

public class TestSystemServiceFactoryProvider implements Provider<SystemServiceFactory> {

    private String wireMockUrl;

    @Inject
    public TestSystemServiceFactoryProvider(@Named("wireMockUrl") String wireMockUrl) {
        this.wireMockUrl = wireMockUrl;
    }

    @Override
    public SystemServiceFactory get() {
        return ClientSystemServiceFactory.buildWithoutPinning(wireMockUrl);
    }
}
