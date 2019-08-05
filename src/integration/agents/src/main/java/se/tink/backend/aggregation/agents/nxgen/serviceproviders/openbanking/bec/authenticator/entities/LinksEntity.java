package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private ScaStatusEntity scaStatus;

    private ScaRedirectEntity scaRedirect;

    private SelfEntity self;

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
