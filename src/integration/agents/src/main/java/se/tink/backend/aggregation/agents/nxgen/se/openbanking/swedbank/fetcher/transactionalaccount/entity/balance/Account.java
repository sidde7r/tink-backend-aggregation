package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Account {

    private String iban;

    public String getIban() {
        return iban;
    }
}
