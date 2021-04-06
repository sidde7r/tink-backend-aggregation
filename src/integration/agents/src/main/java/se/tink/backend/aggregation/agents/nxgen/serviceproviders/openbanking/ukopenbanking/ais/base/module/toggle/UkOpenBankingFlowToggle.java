package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;

@Slf4j
public class UkOpenBankingFlowToggle {
    private final Unleash toggleService;
    private final UnleashContext unleashContext;
    private final String featureToggleName;

    public UkOpenBankingFlowToggle(
            Unleash toggleService, UnleashContext unleashContext, String featureToggleName) {
        this.toggleService = toggleService;
        this.unleashContext = unleashContext;
        this.featureToggleName = featureToggleName;
    }

    public UkOpenBankingFlow takeFlow() {
        UkOpenBankingFlow state =
                isEidasProxyFlow()
                        ? UkOpenBankingFlow.EIDAS_PROXY
                        : UkOpenBankingFlow.SECRET_SERVICE;
        log.info("[UkOpenBanking Toggle] `{}` flow has been chosen.", state);
        return state;
    }

    private boolean isEidasProxyFlow() {
        return toggleService.isEnabled(featureToggleName, unleashContext, false);
    }
}
