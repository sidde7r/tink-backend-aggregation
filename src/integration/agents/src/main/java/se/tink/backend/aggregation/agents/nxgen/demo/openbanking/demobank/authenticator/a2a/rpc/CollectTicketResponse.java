package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollectTicketResponse {
    private String status;
    private String token;

    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }
}
