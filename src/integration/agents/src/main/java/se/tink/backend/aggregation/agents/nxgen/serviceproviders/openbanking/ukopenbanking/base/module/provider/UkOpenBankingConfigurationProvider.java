package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;

public final class UkOpenBankingConfigurationProvider
        implements Provider<UkOpenBankingConfiguration> {

    private final CompositeAgentContext context;

    @Inject
    private UkOpenBankingConfigurationProvider(CompositeAgentContext context) {
        this.context = context;
    }

    @Override
    public UkOpenBankingConfiguration get() {
        return context.getAgentConfigurationController()
                .getAgentConfiguration(UkOpenBankingConfiguration.class)
                .getProviderSpecificConfiguration();
    }
}
