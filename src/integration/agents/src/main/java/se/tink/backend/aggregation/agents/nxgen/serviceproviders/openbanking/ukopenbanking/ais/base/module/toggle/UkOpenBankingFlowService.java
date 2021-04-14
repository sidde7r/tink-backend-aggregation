package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@Slf4j
public class UkOpenBankingFlowService {

    private final UnleashClient unleashClient;
    private final Toggle toggle;

    public UkOpenBankingFlowService(UnleashClient unleashClient, Toggle toggle) {
        this.unleashClient = unleashClient;
        this.toggle = toggle;
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
        return unleashClient.isToggleEnable(toggle);
    }
}
