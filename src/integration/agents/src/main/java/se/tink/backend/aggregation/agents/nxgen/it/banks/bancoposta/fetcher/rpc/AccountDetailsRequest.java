package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsRequest extends BaseRequest {
    private Body body;

    @JsonObject
    public static class Body {
        @JsonProperty("numeroConto")
        private String accountNumber;
    }

    public AccountDetailsRequest(String accountNumber) {
        this.body = new Body();
        this.body.accountNumber = accountNumber;
    }
}
