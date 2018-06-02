package se.tink.backend.rpc;

import io.protostuff.Tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import se.tink.backend.core.TransactionFraudStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionFraudulentRequest {

    @Tag(1)
    private TransactionFraudStatus status;

    public TransactionFraudStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionFraudStatus status) {
        this.status = status;
    }
}
