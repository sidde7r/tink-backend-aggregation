package se.tink.backend.aggregation.agents.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.unleash.UnleashClient;

public class ProdUnleashModule extends AbstractModule {

    @Inject
    @Provides
    UnleashClient getUnleashModule(AgentComponentProvider componentProvider) {
        return componentProvider.getUnleashClient();
    }
}
