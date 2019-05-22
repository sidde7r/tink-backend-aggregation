package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionRequest {
    @JsonProperty("secret")
    private String apiKey;

    private CreateSessionRequest(String apiKey) {
        this.apiKey = apiKey;
    }

    public static CreateSessionRequest createFromApiKey(String apiKey) {
        return new CreateSessionRequest(apiKey);
    }

    public String getApiKey() {
        return apiKey;
    }
}
