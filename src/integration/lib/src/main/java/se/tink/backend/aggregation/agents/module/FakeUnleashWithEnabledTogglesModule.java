package se.tink.backend.aggregation.agents.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient.FakeUnleashClient;
import se.tink.libraries.unleash.UnleashClient;

public class FakeUnleashWithEnabledTogglesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UnleashClient.class).toInstance(new FakeUnleashClient(true));
    }
}
