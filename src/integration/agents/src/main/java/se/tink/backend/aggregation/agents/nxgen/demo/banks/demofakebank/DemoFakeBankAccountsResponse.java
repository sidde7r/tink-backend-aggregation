package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class DemoFakeBankAccountsResponse {
    @JsonProperty("status")
    private String status;
    @JsonProperty("message")
    private String message;
    @JsonProperty("accounts")
    private List<FakeAccount> accounts;

    public DemoFakeBankAccountsResponse() {
    }

    public List<FakeAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<FakeAccount> accounts) {
        this.accounts = accounts;
    }
}
