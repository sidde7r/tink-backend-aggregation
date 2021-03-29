package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;

public final class DynamicUkOpenBankingFlowSwitcherModule extends AbstractModule {

    /**
     * Temporary solution till the feature flag won't be implemented in order to change flow
     * dynamically
     */
    private static final boolean MAGIC_SWITCH_BETWEEN_FLOWS = false;

    @Override
    protected void configure() {
        if (MAGIC_SWITCH_BETWEEN_FLOWS) {
            install(new EidasProxyFlowModule());
        } else {
            install(new SecretServiceFlowModule());
        }
    }
}
