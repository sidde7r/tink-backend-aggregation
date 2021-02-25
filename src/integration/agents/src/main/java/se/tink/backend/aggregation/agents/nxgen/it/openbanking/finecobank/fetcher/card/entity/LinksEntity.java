package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private TransactionsItem transactions;

    public TransactionsItem getTransactions() {
        return transactions;
    }
}
