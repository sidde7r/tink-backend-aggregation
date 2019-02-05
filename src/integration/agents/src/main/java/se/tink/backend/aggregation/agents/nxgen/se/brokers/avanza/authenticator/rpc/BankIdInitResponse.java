package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdInitResponse {
    private String expires;
    private String autostartToken;
    private String transactionId;

    public String getExpires() {
        return expires;
    }

    public String getAutostartToken() {
        return autostartToken;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
