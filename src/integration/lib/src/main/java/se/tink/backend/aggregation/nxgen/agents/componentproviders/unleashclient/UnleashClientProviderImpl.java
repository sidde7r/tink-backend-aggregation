package se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.libraries.unleash.UnleashClient;

public class UnleashClientProviderImpl implements UnleashClientProvider {

    private final UnleashClient unleashClient;

    @Inject
    public UnleashClientProviderImpl(CompositeAgentContext context) {
        this.unleashClient = context.getUnleashClient();
    }

    @Override
    public UnleashClient getUnleashClient() {
        return this.unleashClient;
    }
}
