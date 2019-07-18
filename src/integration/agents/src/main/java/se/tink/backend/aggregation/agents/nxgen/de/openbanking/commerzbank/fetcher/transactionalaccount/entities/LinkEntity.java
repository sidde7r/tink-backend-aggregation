package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    private AccountEntity account;
    private NextEntity nextEntity;
    private LastEntity lastEntity;
    private FirstEntity firstEntity;

    public AccountEntity getAccount() {
        return account;
    }

    public NextEntity getNext() {
        return nextEntity;
    }

    public LastEntity getLast() {
        return lastEntity;
    }

    public FirstEntity getFirst() {
        return firstEntity;
    }
}
