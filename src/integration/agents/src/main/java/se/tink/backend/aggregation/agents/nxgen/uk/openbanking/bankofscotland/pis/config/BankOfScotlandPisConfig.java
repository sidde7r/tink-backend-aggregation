package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.bankofscotland.pis.config;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.PartyToPartyRisk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfiguration;

public class BankOfScotlandPisConfig extends UkOpenBankingPisConfiguration {
    public BankOfScotlandPisConfig(String pisBaseUrl, String wellKnownURL) {
        super(pisBaseUrl, wellKnownURL);
    }

    @Override
    public Risk getPaymentContext() {
        return new PartyToPartyRisk();
    }
}
