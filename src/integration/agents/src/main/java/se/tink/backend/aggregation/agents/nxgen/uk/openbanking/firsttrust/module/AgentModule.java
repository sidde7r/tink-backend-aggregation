package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.firsttrust.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModule;

public final class AgentModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new UkOpenBankingLocalKeySignerModule());
    }
}
