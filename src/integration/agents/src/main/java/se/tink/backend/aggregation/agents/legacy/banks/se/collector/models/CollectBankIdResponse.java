package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.BankIdStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CollectBankIdResponse {
    @JsonProperty("status")
    private String status;

    public BankIdStatus getStatus() {
        switch (status.toUpperCase()) {
            case "COMPLETE":
                return BankIdStatus.DONE;
            case "CANCELLED":
                return BankIdStatus.CANCELLED;
            case "USER_SIGN":
            case "OUTSTANDING_TRANSACTION":
            case "NO_CLIENT":
                return BankIdStatus.WAITING;
            case "EXPIRED_TRANSACTION":
                return BankIdStatus.TIMEOUT;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public abstract boolean isValid();
}
