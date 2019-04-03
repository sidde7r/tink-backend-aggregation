package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;

public enum AccountSubtypeEntity {

    CURRENT_ACCOUNT(CrosskeyBaseConstants.AccountType.CURRENT_ACCOUNT),
    CREDIT_CARD(CrosskeyBaseConstants.AccountType.CREDIT_CARD);

    private final String key;

    AccountSubtypeEntity(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
