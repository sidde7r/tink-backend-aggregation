package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitRequest {
    private String subject;
    private String apiKey;

    public BankIdInitRequest(String subject, String apiKey) {
        this.subject = subject;
        this.apiKey = apiKey;
    }

    public String getSubject() {
        return subject;
    }
}
