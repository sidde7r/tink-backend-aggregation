package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NextRequestEntity {
    @JsonProperty private String method;
    @JsonProperty private String wait;
    @JsonProperty private String uri;

    @JsonIgnore
    public String getUri() {
        return uri;
    }
}
