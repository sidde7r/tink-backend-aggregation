package se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient;

import se.tink.libraries.unleash.UnleashClient;

public class FakeUnleashClientProviderImpl implements UnleashClientProvider {

    @Override
    public UnleashClient getUnleashClient() {
        return new FakeUnleashClient(true);
    }
}
