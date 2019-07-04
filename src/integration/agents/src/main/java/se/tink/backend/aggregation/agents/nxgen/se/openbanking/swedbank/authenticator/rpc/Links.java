package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private ScaStatus scaStatus;

    private ScaRedirect scaRedirect;

    private Status status;

    public ScaStatus getScaStatus() {
        return scaStatus;
    }

    public ScaRedirect getScaRedirect() {
        return scaRedirect;
    }

    public Status getStatus() {
        return status;
    }
}
