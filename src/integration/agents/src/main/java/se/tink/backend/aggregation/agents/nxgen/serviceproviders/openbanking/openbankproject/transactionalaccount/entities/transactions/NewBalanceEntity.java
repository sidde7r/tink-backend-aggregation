package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NewBalanceEntity {

    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
