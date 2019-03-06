package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FakeAccountsResponse {

    @JsonProperty
    private FakeAccounts fakeAccounts;

    public FakeAccounts getFakeAccounts() {
        return fakeAccounts;
    }
}
