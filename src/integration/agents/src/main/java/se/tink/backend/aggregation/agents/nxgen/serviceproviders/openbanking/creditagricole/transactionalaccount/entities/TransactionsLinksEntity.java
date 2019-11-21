package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {
    private LinkDetailsEntity balances;
    private LinkDetailsEntity first;
    private LinkDetailsEntity last;
    private LinkDetailsEntity next;
    private LinkDetailsEntity parentlist;
    private LinkDetailsEntity prev;
    private LinkDetailsEntity self;

    public LinkDetailsEntity getNext() {
        return next;
    }

    public LinkDetailsEntity getSelf() {
        return self;
    }
}
