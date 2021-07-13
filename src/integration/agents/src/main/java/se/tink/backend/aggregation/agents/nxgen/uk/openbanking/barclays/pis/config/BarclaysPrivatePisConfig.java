package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.pis.config;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.PartyToPartyRisk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;

public class BarclaysPrivatePisConfig extends UkOpenBankingPisConfiguration {

    public BarclaysPrivatePisConfig(String pisBaseUrl, String wellKnownURL) {
        super(pisBaseUrl, wellKnownURL);
    }

    @Override
    public Risk getPaymentContext() {
        return new PartyToPartyRisk();
    }
}
