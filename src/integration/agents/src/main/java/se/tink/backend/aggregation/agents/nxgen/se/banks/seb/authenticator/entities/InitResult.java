package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitResult {
    @JsonProperty("INIT_TIME")
    private String initTime;

    @JsonProperty("INIT_RESULT")
    private String initResult;

    @JsonIgnore
    public String getInitResult() {
        return initResult;
    }
}
