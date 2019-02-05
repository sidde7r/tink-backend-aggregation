package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountsResponse {

    @JsonProperty("hasClosedAccount")
    private boolean hasClosedAccount;

    @JsonProperty("konton")
    private List<AccountEntity> accounts;

    public boolean hasClosedAccount() {
        return hasClosedAccount;
    }

    public void setHasClosedAccount(boolean hasClosedAccount) {
        this.hasClosedAccount = hasClosedAccount;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
