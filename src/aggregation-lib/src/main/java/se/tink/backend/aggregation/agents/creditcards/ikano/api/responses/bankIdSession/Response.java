package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    @JsonProperty("_Session")
    public Session session;
}
