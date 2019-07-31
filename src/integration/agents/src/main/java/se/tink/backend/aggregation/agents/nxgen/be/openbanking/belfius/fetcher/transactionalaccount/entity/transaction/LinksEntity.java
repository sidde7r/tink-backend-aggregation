package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private AccountEntity account;
    private NextEntity next;
    private SelfEntity self;

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public NextEntity getNext() {
        return next;
    }

    public void setNext(NextEntity next) {
        this.next = next;
    }

    public SelfEntity getSelf() {
        return self;
    }

    public void setSelf(SelfEntity self) {
        this.self = self;
    }
}
