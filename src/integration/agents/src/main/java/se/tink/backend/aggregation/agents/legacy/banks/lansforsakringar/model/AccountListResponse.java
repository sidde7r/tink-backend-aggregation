package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse {
    protected List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
