package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds.pis.config;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;

public class LloydsPisConfig extends UkOpenBankingPisConfiguration {
    public LloydsPisConfig(String pisBaseUrl, String wellKnownURL) {
        super(pisBaseUrl, wellKnownURL);
    }

    @Override
    public boolean useOtherPaymentContext() {
        return false;
    }
}
