package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.UkOpenBankingToggleModule;

public final class DynamicUkOpenBankingFlowSwitcherModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new UkOpenBankingToggleModule());
        bind(UkOpenBankingFlowFacade.class)
                .toProvider(UkOpenBankingFlowFactory.class)
                .in(Scopes.SINGLETON);
    }
}
