package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DemoFakeBankAccountBody {
    @JsonProperty private String username;
    @JsonProperty private String token;

    public DemoFakeBankAccountBody(String username, String token) {
        this.username = username;
        this.token = token;
    }
}
