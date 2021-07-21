package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href balances;
    private Href transactions;

    public String getBalances() {
        return balances.getHref();
    }

    public String getTransactions() {
        return transactions.getHref();
    }
}
