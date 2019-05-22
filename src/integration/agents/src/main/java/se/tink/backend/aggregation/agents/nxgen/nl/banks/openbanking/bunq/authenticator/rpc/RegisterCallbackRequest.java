package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterCallbackRequest {
    private String url;

    public RegisterCallbackRequest(String url) {
        this.url = url;
    }
}
