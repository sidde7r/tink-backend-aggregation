package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.pis.config;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;

public class NationwidePisConfig extends UkOpenBankingPisConfiguration {
    public NationwidePisConfig(String pisBaseUrl, String wellKnownURL) {
        super(pisBaseUrl, wellKnownURL);
    }

    @Override
    public boolean useMaxAge() {
        return false;
    }
}
