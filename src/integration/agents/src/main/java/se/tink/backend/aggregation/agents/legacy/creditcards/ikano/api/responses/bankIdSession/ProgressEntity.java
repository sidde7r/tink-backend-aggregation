package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProgressEntity {
    @JsonProperty("_ProgressCode")
    private String progressCode;

    @JsonProperty("_ProgressMessage")
    private String progressMessage;

    public String getProgressCode() {
        return progressCode;
    }

    public String getProgressMessage() {
        return progressMessage;
    }
}
