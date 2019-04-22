package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {
    private String balances;
    private String transactions;

    public String getTransactions() {
        return transactions;
    }
    public String getBalances() {
        return balances;
    }
}
