package se.tink.backend.aggregation.agents.brokers.lysa.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StartBankIdResponse {
    private String autostartToken;
    private String transactionId;

    public String getAutostartToken() {
        return autostartToken;
    }

    public void setAutostartToken(String autostartToken) {
        this.autostartToken = autostartToken;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
