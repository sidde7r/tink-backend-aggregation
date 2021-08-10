package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.halifax.pis.config;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.PartyToPartyRisk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;

public class HalifaxPisConfig extends UkOpenBankingPisConfiguration {
    public HalifaxPisConfig(String pisBaseUrl, String wellKnownURL) {
        super(pisBaseUrl, wellKnownURL);
    }

    @Override
    public Risk getPaymentContext() {
        return new PartyToPartyRisk();
    }
}
