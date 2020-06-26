package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateTicketResponse {
    private String deeplinkUrl;
    private String token; // TODO: fix this

    public String getDeeplinkUrl() {
        return deeplinkUrl;
    }

    public String getToken() {
        return token;
    }

    // TODO: This should be a separate object
    public void setToken(String token) {
        this.token = token;
    }
}
