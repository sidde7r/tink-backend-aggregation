package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    private Href transactions;
    private Href balances;
    private Href account;
    private Href self;

    public Href getSelf() {
        return self;
    }

    public Href getAccount() {
        return account;
    }

    public Href getBalances() {
        return balances;
    }

    public Href getTransactions() {
        return transactions;
    }
}
