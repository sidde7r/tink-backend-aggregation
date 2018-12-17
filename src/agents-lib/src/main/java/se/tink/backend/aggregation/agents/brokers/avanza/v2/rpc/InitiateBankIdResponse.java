package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdResponse {
    private String transactionId;
    private String autostartToken;
    private String expires;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAutostartToken() {
        return autostartToken;
    }

    public void setAutostartToken(String autostartToken) {
        this.autostartToken = autostartToken;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }
}
