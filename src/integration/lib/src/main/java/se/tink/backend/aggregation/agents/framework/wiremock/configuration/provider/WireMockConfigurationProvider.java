package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import java.util.HashMap;
import java.util.Map;
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
            // TODO: Fix it later by reading it from a configuration file maybe
            Map<String, String> mockCallbackData = new HashMap<>();
            mockCallbackData.put("code", "DUMMY_AUTH_CODE");
            return WireMockConfiguration.builder().setCallbackData(mockCallbackData).build();
        } catch (IllegalStateException e) {
            throw new ProvisionException(e.getMessage());
        }
    }
}
