package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String username;
    private String iban;
    private String accountType;
    private String currency;
    private BalanceEntity balance;
    private BalanceEntity available;
    private String accountTypeCode;

    @JsonProperty("bezeichnung")
    public String getUsername() {
        return username;
    }

    public String getIban() {
        return iban;
    }

    @JsonProperty("kontoartBezeichnung")
    public String getAccountType() {
        return accountType;
    }

    @JsonProperty("waehrung")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("kontostand")
    public BalanceEntity getBalance() {
        return balance;
    }

    @JsonProperty("verfuegbarerBetrag")
    public BalanceEntity getAvailable() {
        return available;
    }

    @JsonProperty("kontoart")
    public String getAccountTypeCode() {
        return accountTypeCode;
    }
}
