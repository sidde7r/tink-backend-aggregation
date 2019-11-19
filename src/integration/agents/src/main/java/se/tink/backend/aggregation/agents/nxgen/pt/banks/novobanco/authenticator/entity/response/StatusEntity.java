package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatusEntity {

    public int getSeverity() {
        return severity;
    }

    public int getCode() {
        return code;
    }

    @JsonProperty("Severidade")
    private int severity;

    @JsonProperty("Codigo")
    private int code;
}
