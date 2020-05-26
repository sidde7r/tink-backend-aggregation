package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitSamlResponse {
    @JsonProperty("request_url")
    private String requestUrl;

    public String getRequestUrl() {
        return requestUrl;
    }
}
