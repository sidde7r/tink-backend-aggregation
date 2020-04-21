package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;

public final class WireMockConfigurationProvider implements Provider<WireMockConfiguration> {

    private final FakeBankSocket fakeBankSocket;

    @Inject
    private WireMockConfigurationProvider(final FakeBankSocket fakeBankSocket) {
        this.fakeBankSocket = fakeBankSocket;
    }

    @Override
    public WireMockConfiguration get() {
        try {
            return WireMockConfiguration.builder().build();
        } catch (IllegalStateException e) {
            throw new ProvisionException(e.getMessage());
        }
    }
}
