package se.tink.backend.aggregation.agents.contexts;

import se.tink.libraries.unleash.UnleashClient;

public interface UnleashContext {

    UnleashClient getUnleashClient();

    void setUnleashClient(UnleashClient unleashClient);
}
