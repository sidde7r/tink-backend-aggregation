package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdOrderRequest {

    private String subject;
    private boolean useAnotherDevice = true;

    public BankIdOrderRequest(String subject) {
        this.subject = subject;
    }
}
