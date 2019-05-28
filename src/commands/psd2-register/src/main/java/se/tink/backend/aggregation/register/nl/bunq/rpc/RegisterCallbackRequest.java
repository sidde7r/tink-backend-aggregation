package se.tink.backend.aggregation.register.nl.bunq.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterCallbackRequest {
    private String url;

    public RegisterCallbackRequest(String url) {
        this.url = url;
    }
}
