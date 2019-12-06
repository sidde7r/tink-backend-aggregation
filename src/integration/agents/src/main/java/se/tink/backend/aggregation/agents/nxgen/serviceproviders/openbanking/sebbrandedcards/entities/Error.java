package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Error {

    @JsonProperty("developerMessage")
    private String developerMessage;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("correlationId")
    private String correlationId;

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
