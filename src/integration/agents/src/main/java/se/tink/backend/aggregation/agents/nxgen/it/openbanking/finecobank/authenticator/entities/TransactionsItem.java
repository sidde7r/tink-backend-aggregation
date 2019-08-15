package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsItem {

    private String maskedPan;
    private String currency;
    private String iban;
    private String msisdn;
    private String pan;

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIban() {
        return iban;
    }
}
