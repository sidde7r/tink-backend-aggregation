package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessage {
    @JsonProperty private String field;
    @JsonProperty private String message;
    @JsonProperty private String errorCode;

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
