package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    @JsonProperty("Code")
    private String code;

    public String getCode() {
        return code;
    }
}
