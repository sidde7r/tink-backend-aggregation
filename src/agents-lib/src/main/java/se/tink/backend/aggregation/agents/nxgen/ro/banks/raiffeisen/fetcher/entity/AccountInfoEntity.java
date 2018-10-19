package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {
    private String accountNumber;
    private String iban;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }
}
