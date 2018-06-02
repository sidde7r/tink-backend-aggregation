package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountsBody {
    @JsonProperty("Accounts")
    private AccountListEntity accounts;

    public AccountListEntity getAccounts() {
        return accounts;
    }

    public void setAccounts(AccountListEntity accounts) {
        this.accounts = accounts;
    }
}
