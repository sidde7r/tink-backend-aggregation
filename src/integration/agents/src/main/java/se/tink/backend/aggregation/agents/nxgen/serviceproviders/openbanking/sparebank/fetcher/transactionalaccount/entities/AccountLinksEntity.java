package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {
    private LinkEntity balances;
    private LinkEntity transactions;

    public String getTransactionLink() {
        return transactions.getHref();
    }

    public String getBalanceLink() {
        return balances.getHref();
    }
}
