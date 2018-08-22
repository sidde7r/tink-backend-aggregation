package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionModel {
    private Object error;
    @JsonProperty("result")
    private SessionResultEntity result;

    public SessionResultEntity getResult() {
        return result;
    }

    public Object getError() {
        return error;
    }
}
