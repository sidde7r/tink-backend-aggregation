package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.aib.pis.config;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;

public class AibPisConfig extends UkOpenBankingPisConfiguration {
    public AibPisConfig(String pisBaseUrl, String wellKnownURL) {
        super(pisBaseUrl, wellKnownURL);
    }

    /**
     * AIB and Firsttrust is in the same group without any ambassadors AIB and Firsttrust always
     * reject the funds confimation with 403 Exempted those two from funds confirmation
     *
     * @return false
     */
    @Override
    public boolean compatibleWithFundsConfirming() {
        return false;
    }
}
