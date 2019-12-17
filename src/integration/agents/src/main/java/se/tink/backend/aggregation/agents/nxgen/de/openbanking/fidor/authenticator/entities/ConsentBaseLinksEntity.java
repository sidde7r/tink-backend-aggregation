package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseLinksEntity {
    @JsonProperty private HrefEntity scaRedirect;
    @JsonProperty private HrefEntity status;
    @JsonProperty private HrefEntity scaStatus;
    @JsonProperty private HrefEntity self;

    @JsonIgnore
    public HrefEntity getScaRedirect() {
        return scaRedirect;
    }

    @JsonIgnore
    public HrefEntity getStatus() {
        return status;
    }

    @JsonIgnore
    public HrefEntity getScaStatus() {
        return scaStatus;
    }

    @JsonIgnore
    public HrefEntity getSelf() {
        return self;
    }
}
