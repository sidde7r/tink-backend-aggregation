package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.yorkshire;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;

public class YorkshireConfigurationProvider implements Provider<UkOpenBankingConfiguration> {

    private final CompositeAgentContext context;

    @Inject
    public YorkshireConfigurationProvider(CompositeAgentContext context) {
        this.context = context;
    }

    @Override
    public UkOpenBankingConfiguration get() {
        return context.getAgentConfigurationController()
                .getAgentConfiguration(YorkshireClientConfiguration.class)
                .getProviderSpecificConfiguration();
    }
}
