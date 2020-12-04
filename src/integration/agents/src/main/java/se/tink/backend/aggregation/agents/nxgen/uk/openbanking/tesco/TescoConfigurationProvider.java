package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tesco;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;

public class TescoConfigurationProvider
        implements Provider<UkOpenBankingClientConfigurationAdapter> {

    private final CompositeAgentContext context;

    @Inject
    public TescoConfigurationProvider(CompositeAgentContext context) {
        this.context = context;
    }

    @Override
    public UkOpenBankingClientConfigurationAdapter get() {
        return context.getAgentConfigurationController()
                .getAgentConfiguration(TescoClientConfiguration.class)
                .getProviderSpecificConfiguration();
    }
}
