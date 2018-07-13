package se.tink.backend.aggregation.agents.creditcards.supremecard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {
    private AccountEntity data;
    private boolean success;

    public AccountEntity getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setData(AccountEntity data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
