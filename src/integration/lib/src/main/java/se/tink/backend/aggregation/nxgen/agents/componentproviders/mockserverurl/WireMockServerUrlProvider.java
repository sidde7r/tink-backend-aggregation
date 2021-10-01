package se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;

public class WireMockServerUrlProvider implements MockServerUrlProvider {
    private final String mockServerUrl;

    @Inject
    public WireMockServerUrlProvider(final FakeBankSocket fakeBankSocket) {
        this.mockServerUrl = "http://" + fakeBankSocket.getHttpHost();
    }

    @Override
    public Optional<String> getMockServerUrl() {
        return Optional.of(mockServerUrl);
    }
}
