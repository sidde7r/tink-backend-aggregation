package se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl;

import java.util.Optional;

public class EmptyMockServerUrlProvider implements MockServerUrlProvider {

    @Override
    public Optional<String> getMockServerUrl() {
        return Optional.empty();
    }
}
