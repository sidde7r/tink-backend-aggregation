package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private HrefEntity scaStatus;

    private HrefEntity scaRedirect;

    private HrefEntity self;

    private HrefEntity status;

    public HrefEntity getScaStatus() {
        return scaStatus;
    }

    public HrefEntity getScaRedirect() {
        return scaRedirect;
    }

    public HrefEntity getSelf() {
        return self;
    }

    public HrefEntity getStatus() {
        return status;
    }
}
