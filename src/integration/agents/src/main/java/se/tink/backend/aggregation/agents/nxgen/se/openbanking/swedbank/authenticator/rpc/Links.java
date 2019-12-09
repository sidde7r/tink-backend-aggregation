package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private HrefEntity scaStatus;

    private HrefEntity scaRedirect;

    private HrefEntity status;

    public HrefEntity getScaStatus() {
        return scaStatus;
    }

    public HrefEntity getHrefEntity() {
        return scaRedirect;
    }

    public HrefEntity getStatus() {
        return status;
    }
}
