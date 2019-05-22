package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddOauthClientIdRequest {
    private String status;

    public AddOauthClientIdRequest(String status) {
        this.status = status;
    }

    public static class Status {
        public static final String ACTIVE = "ACTIVE";
        public static final String CANCELLED = "CANCELLED";
    }
}
