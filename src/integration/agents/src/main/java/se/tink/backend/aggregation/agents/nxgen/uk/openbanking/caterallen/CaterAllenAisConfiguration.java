package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.caterallen;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;

public class CaterAllenAisConfiguration extends UkOpenBankingAisConfiguration {

    public CaterAllenAisConfiguration(Builder builder) {
        super(builder);
    }

    @Override
    public boolean isFetchingTransactionsFromTheNewestToTheOldest() {
        return false;
    }
}
