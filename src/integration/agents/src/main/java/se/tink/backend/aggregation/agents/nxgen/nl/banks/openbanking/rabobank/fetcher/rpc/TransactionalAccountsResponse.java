package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalAccountsResponse {

    @JsonProperty("accounts")
    private List<AccountsItem> accounts;

    public List<AccountsItem> getAccounts() {
        return accounts;
    }

    public void setAccounts(final List<AccountsItem> accounts) {
        this.accounts = accounts;
    }
}
