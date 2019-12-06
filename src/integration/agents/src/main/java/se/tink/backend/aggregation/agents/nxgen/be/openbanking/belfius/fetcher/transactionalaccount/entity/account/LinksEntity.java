package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private SelfEntity self;
    private TransactionsEntity transactions;

    public SelfEntity getSelf() {
        return self;
    }

    public void setSelf(SelfEntity self) {
        this.self = self;
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public void setTransactions(TransactionsEntity transactions) {
        this.transactions = transactions;
    }
}
