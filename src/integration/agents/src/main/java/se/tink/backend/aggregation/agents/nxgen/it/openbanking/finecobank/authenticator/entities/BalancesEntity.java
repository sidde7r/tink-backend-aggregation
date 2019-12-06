package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesEntity {

    private String iban;
    private String currency;

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }
}
