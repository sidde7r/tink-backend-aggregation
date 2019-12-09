package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItem {

    private String iban;
    private String maskedPan;
    private String currency;
    private String pan;
    private String msisdn;

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMaskedPan() {
        return maskedPan;
    }
}
