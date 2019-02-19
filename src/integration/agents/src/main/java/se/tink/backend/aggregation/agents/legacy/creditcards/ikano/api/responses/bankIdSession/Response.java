package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Response {
    @JsonProperty("_Progress")
    private ProgressEntity progress;
    @JsonProperty("_Session")
    public Session session;

    public ProgressEntity getProgress() {
        return progress;
    }

    public Session getSession() {
        return session;
    }
}
