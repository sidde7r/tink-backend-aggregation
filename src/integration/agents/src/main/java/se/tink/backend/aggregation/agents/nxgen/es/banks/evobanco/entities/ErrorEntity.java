package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {

    @JsonProperty("errorCode")
    private String code;

    @JsonProperty("description")
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
