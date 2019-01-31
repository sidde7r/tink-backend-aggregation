package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.BaseResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdSession extends BaseResponse {
    @JsonProperty("Response")
    public Response response;

    public String getSessionId() {
        return response.session.getSessionId();
    }

    public String getSessionKey() {
        return response.session.getSessionKey();
    }

    public boolean hasSession() {
        return response != null && response.session != null;
    }
}