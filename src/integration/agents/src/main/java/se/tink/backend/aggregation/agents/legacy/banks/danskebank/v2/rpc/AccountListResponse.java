package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse extends AbstractResponse {
    @JsonProperty("Sorted")
    protected boolean sorted;

    @JsonProperty("Accounts")
    protected List<AccountEntity> accounts;

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    public List<AccountEntity> getAccounts() {
        return accounts != null ? accounts : Collections.<AccountEntity>emptyList();
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
