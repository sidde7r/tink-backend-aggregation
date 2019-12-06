package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity balances;
    private LinkEntity transactions;
    private LinkEntity next;

    public String getBalanceLink() {
        return balances.getHref();
    }

    public String getTransactionLink() {
        return transactions.getHref();
    }

    public boolean hasNext() {
        return next != null && next.hasNext();
    }

    public String getNext() {
        return next.getHref();
    }
}
