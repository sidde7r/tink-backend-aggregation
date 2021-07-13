package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href balances;
    private Href transactions;

    public String getBalances() {
        return String.valueOf(balances);
    }

    public String getTransactions() {
        return String.valueOf(transactions);
    }
}
