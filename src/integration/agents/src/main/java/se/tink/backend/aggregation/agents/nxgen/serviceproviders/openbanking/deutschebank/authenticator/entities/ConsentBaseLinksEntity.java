package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseLinksEntity {
    private HrefEntity scaRedirect;
    private HrefEntity status;
    private HrefEntity scaStatus;
    private HrefEntity self;

    public HrefEntity getScaRedirect() {
        return scaRedirect;
    }

    public HrefEntity getStatus() {
        return status;
    }

    public HrefEntity getScaStatus() {
        return scaStatus;
    }

    public HrefEntity getSelf() {
        return self;
    }
}
