package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class FakeAccounts {

    @JsonProperty
    private List<FakeAccount> accounts;

    public FakeAccounts(List<FakeAccount> accounts) {
        this.accounts = accounts;
    }

    public List<FakeAccount> getAccounts() {
        return accounts;
    }
}
