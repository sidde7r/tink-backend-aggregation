package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfoResponse {
    private Boolean success;
    private AccountInfoEntity data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public AccountInfoEntity getData() {
        return data;
    }

    public void setData(AccountInfoEntity data) {
        this.data = data;
    }
}
