package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

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
