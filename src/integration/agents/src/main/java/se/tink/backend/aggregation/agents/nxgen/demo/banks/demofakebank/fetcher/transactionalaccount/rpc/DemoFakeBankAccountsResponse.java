package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.entities.FakeAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class DemoFakeBankAccountsResponse {
    @JsonProperty
    private String status;
    @JsonProperty
    private String message;
    @JsonProperty
    private List<FakeAccount> accounts;

    public DemoFakeBankAccountsResponse() {}

    public List<FakeAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<FakeAccount> accounts) {
        this.accounts = accounts;
    }
}
