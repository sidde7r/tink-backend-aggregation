package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.entities;

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
}
