package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkEntity balances;
    private LinkEntity transactions;
    private LinkEntity next;

    public String getBalancesUrl() {
        return balances != null ? balances.getHref() : null;
    }

    public String getTransactionsUrl() {
        return transactions.getHref();
    }

    public String getNext() {
        return next.getHref();
    }

    public boolean hasNext() {
        return next != null;
    }
}
