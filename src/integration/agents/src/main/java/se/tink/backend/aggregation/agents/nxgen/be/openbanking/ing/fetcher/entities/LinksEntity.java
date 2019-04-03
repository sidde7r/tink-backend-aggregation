package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkEntity balances;
    private LinkEntity transactions;

    public String getBalancesUrl() {
        return balances.getHref();
    }

    public String getTransactionsUrl() {
        return transactions.getHref();
    }
}
