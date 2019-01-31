package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse {
    private List<AccountEntity> bankAccounts;

    public List<AccountEntity> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<AccountEntity> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }
}
