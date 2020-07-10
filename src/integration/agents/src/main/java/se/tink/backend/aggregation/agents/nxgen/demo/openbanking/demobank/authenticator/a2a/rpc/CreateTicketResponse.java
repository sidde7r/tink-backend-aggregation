package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateTicketResponse {
    private String deeplinkUrl;
    private String ticket;

    public String getDeeplinkUrl() {
        return deeplinkUrl;
    }

    public String getTicket() {
        return ticket;
    }
}
