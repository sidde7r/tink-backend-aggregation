package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UKOpenBankingLocalKeySignerModule;

public final class BarclaysWireMockTestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new UKOpenBankingLocalKeySignerModule());
    }
}
