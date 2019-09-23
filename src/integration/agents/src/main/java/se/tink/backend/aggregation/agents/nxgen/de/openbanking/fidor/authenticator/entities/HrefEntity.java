package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HrefEntity {

    @JsonProperty private String href;

    @JsonIgnore
    public String getHref() {
        return href;
    }
}
