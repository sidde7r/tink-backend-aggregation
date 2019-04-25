package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessInfoEntity {

    private String currency;
    private String iban;

    public AccessInfoEntity(String currency, String iban) {
        this.currency = currency;
        this.iban = iban;
    }
}
