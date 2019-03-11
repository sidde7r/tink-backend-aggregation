package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.entities.FakeAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        return Objects.nonNull(accounts) ? accounts : Collections.emptyList();
    }

    public void setAccounts(List<FakeAccount> accounts) {
        this.accounts = accounts;
    }
}
