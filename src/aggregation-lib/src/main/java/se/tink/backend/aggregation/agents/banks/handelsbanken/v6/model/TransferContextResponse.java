package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferContextResponse extends AbstractResponse {
    private AccountListResponse fromAccounts;
    private AccountListResponse toAccounts;

    public AccountListResponse getFromAccounts() {
        return fromAccounts;
    }

    public void setFromAccounts(AccountListResponse fromAccounts) {
        this.fromAccounts = fromAccounts;
    }

    public AccountListResponse getToAccounts() {
        return toAccounts;
    }

    public void setToAccounts(AccountListResponse toAccounts) {
        this.toAccounts = toAccounts;
    }
}
