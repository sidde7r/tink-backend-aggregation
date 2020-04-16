package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.InetSocketAddress;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;

public final class WireMockConfigurationProvider implements Provider<WireMockConfiguration> {

    private final InetSocketAddress fakeBankSocket;

    @Inject
    private WireMockConfigurationProvider(@FakeBankSocket final InetSocketAddress fakeBankSocket) {
        this.fakeBankSocket = fakeBankSocket;
    }

    @Override
    public WireMockConfiguration get() {
        return WireMockConfiguration.builder(
                        fakeBankSocket.getHostString() + ":" + fakeBankSocket.getPort())
                .build();
    }
}
