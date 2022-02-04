package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.rpc;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MitIdCodeAppPollResponse {

    private String status;
    private boolean confirmation;

    public boolean isStillPolling() {
        return equalsIgnoreCase(status, "timeout");
    }

    public boolean isExpired() {
        return equalsIgnoreCase(status, "expired");
    }

    public boolean isRejected() {
        return equalsIgnoreCase(status, "ok") && !confirmation;
    }

    public boolean isAccepted() {
        return equalsIgnoreCase(status, "ok") && confirmation;
    }
}
