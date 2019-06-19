package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Account account;
    private Next next;
    private Self self;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Next getNext() {
        return next;
    }

    public void setNext(Next next) {
        this.next = next;
    }

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }
}
