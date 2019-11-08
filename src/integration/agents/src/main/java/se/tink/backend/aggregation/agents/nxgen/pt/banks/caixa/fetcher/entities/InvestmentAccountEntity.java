package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class InvestmentAccountEntity {
    private String fullAccountKey;
    private String accountNumber;
    private String iban;
    private String accountType;
    private String currency;
    private String description;

    public String getFullAccountKey() {
        return fullAccountKey;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }
}
