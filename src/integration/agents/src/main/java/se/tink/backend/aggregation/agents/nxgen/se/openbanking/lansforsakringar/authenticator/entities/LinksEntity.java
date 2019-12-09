package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private HrefEntity scaStatus;

    private HrefEntity scaOAuth;

    private HrefEntity self;

    private HrefEntity startAuthorisation;

    private HrefEntity status;

    public HrefEntity getScaStatus() {
        return scaStatus;
    }

    public HrefEntity getScaOAuth() {
        return scaOAuth;
    }

    public HrefEntity getSelf() {
        return self;
    }

    public HrefEntity getStartAuthorisation() {
        return startAuthorisation;
    }

    public HrefEntity getStatus() {
        return status;
    }
}
