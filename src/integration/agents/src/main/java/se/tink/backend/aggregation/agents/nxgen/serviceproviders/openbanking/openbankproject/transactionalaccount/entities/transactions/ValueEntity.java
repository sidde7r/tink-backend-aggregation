package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {

    private String currency;
    private Number amount;

    public String getCurrency() {
        return currency;
    }

    public Number getAmount() {
        return amount;
    }
}
