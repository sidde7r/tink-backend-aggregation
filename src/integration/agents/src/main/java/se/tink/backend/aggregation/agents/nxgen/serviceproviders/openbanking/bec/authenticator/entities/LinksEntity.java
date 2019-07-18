package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("scaStatus")
    private ScaStatusEntity scaStatus;

    @JsonProperty("scaRedirect")
    private ScaRedirectEntity scaRedirect;

    @JsonProperty("self")
    private SelfEntity self;

    @JsonProperty("status")
    private StatusEntity status;

    public ScaStatusEntity getScaStatus() {
        return scaStatus;
    }

    public ScaRedirectEntity getScaRedirect() {
        return scaRedirect;
    }

    public SelfEntity getSelf() {
        return self;
    }

    public StatusEntity getStatus() {
        return status;
    }
}
