package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessIdentifier {
    private String iban;
    private String currency = "EUR";

    public ConsentAccessIdentifier(String iban) {
        this.iban = iban;
    }
}
