package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;

public class DanskebankEUConfigurationProvider implements Provider<DanskebankEUConfiguration> {

    private final CompositeAgentContext context;

    @Inject
    private DanskebankEUConfigurationProvider(CompositeAgentContext context) {
        this.context = context;
    }

    @Override
    public DanskebankEUConfiguration get() {
        return context.getAgentConfigurationController()
                .getAgentConfiguration(DanskebankEUConfiguration.class)
                .getProviderSpecificConfiguration();
    }
}
