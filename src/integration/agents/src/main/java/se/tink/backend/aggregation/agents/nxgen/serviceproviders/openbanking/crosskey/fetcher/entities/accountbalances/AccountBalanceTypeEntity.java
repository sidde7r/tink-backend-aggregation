package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;

public enum AccountBalanceTypeEntity {

    BOOKED(CrosskeyBaseConstants.AccountBalanceType.BOOKED),
    AVAILABLE(CrosskeyBaseConstants.AccountBalanceType.AVAILABLE);

    private final String key;

    AccountBalanceTypeEntity(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
