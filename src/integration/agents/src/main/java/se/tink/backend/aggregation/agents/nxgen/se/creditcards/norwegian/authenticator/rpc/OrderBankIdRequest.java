package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OrderBankIdRequest {
    private final String subject;
    private final boolean useAnotherDevice = true;

    public OrderBankIdRequest(String subject) {
        this.subject = subject;
    }
}
