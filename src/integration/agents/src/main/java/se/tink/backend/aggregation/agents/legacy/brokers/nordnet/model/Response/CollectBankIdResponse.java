package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import se.tink.backend.aggregation.agents.BankIdStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectBankIdResponse {
    @JsonProperty("progressStatus")
    private String status;

    private String completeUrl;

    public BankIdStatus getStatus() {
        switch (status) {
            case "USER_SIGN":
            case "OUTSTANDING_TRANSACTION":
                return BankIdStatus.WAITING;
            case "COMPLETE":
                return BankIdStatus.DONE;
            case "NO_CLIENT":
                return BankIdStatus.NO_CLIENT;
            default:
                throw new IllegalStateException(
                        "Unknown error detected while collecting BankID status: " + status);
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    public void setCompleteUrl(String completeUrl) {
        this.completeUrl = completeUrl;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("progressStatus", status)
                .add("completeUrl", completeUrl)
                .toString();
    }
}
