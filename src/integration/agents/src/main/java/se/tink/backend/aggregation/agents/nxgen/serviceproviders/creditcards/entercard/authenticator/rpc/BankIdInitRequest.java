package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class BankIdInitRequest {
    private String subject;
    private boolean useAnotherDevice = true;

    public BankIdInitRequest(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
}
