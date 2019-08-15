package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessInfoEntity {
    private String bban;
    private String currency;
    private String iban;
    private String maskedPan;
    private String msisdn;

    public AccessInfoEntity(String currency, String iban) {
        this.currency = currency;
        this.iban = iban;
    }
}
