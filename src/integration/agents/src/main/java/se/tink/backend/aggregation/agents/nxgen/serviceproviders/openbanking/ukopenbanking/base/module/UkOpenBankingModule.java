package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider.UkOpenBankingConfigurationProvider;

public final class UkOpenBankingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UkOpenBankingConfiguration.class)
                .toProvider(UkOpenBankingConfigurationProvider.class)
                .in(Scopes.SINGLETON);
    }
}
