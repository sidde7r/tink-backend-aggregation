package se.tink.backend.aggregation.register.nl.bunq.rpc;

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
