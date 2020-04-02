package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UKOpenBankingLocalKeySignerModule;

public final class AgentModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new UKOpenBankingLocalKeySignerModule());
    }
}
