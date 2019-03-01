package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class TransactionalAccountsResponse {

    @JsonProperty("accounts")
    private List<AccountsItem> accounts;

    public void setAccounts(final List<AccountsItem> accounts) {
        this.accounts = accounts;
    }

    public List<AccountsItem> getAccounts() {
        return accounts;
    }
}
