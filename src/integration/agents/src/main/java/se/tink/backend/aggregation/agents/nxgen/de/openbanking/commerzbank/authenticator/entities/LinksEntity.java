package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private ScaStatusEntity scaStatus;
    private ScaOAuthEntity scaOAuth;
    private SelfEntity self;
    private StatusEntity status;

    public ScaStatusEntity getScaStatusEntity() {
        return scaStatus;
    }

    public ScaOAuthEntity getScaOAuthEntity() {
        return scaOAuth;
    }

    public SelfEntity getSelfEntity() {
        return self;
    }

    public StatusEntity getStatusEntity() {
        return status;
    }
}
