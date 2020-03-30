package se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider;

import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;

public final class WireMockConfigurationProvider implements Provider<WireMockConfiguration> {

    @Override
    public WireMockConfiguration get() {
        return WireMockConfiguration.builder("localhost:10000").build();
    }
}
