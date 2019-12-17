package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseLinksEntity {
    @JsonProperty private Href scaRedirect;
    @JsonProperty private Href status;
    @JsonProperty private Href scaStatus;
    @JsonProperty private Href self;

    @JsonIgnore
    public Href getScaRedirect() {
        return scaRedirect;
    }

    @JsonIgnore
    public Href getStatus() {
        return status;
    }

    @JsonIgnore
    public Href getScaStatus() {
        return scaStatus;
    }

    @JsonIgnore
    public Href getSelf() {
        return self;
    }
}
