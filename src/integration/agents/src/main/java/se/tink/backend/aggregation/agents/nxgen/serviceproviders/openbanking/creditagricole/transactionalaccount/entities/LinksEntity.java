package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LinksEntity {
    private LinkDetailsEntity balances;
    private LinkDetailsEntity beneficiaries;
    private LinkDetailsEntity first;
    private LinkDetailsEntity last;
    private LinkDetailsEntity next;
    private LinkDetailsEntity prev;
    private LinkDetailsEntity self;
    private LinkDetailsEntity transactions;
    private LinkDetailsEntity endUserIdentity;

    public boolean hasBalances() {
        return balances != null;
    }

    public boolean hasTransactions() {
        return transactions != null;
    }

    public boolean hasEndUserIdentity() {
        return endUserIdentity != null;
    }

    public boolean hasBeneficiaries() {
        return beneficiaries != null;
    }
}
