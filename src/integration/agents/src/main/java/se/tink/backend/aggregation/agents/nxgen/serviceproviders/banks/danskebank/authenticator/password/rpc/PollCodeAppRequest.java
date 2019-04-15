package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollCodeAppRequest {
    private String ticket;

    public PollCodeAppRequest(String ticket) {
        this.ticket = ticket;
    }
}
