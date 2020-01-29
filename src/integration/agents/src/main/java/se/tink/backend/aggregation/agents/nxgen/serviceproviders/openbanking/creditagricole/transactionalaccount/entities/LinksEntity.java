package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkDetailsEntity balances;
    private String beneficiaries;
    private LinkDetailsEntity first;
    private LinkDetailsEntity last;
    private LinkDetailsEntity next;
    private LinkDetailsEntity prev;
    private LinkDetailsEntity self;
    private LinkDetailsEntity transactions;

    public boolean hasBalances() {
        return balances != null;
    }

    public boolean hasTransactions() {
        return transactions != null;
    }
}
