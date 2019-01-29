package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import se.tink.backend.aggregation.agents.BankIdStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdPollResponse {

    @JsonProperty("status")
    private String status = "";

    @JsonProperty("message")
    private String message = "";

    @JsonProperty("client")
    private String client;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("ocsp")
    private String ocsp;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getOcsp() {
        return ocsp;
    }

    public void setOcsp(String ocsp) {
        this.ocsp = ocsp;
    }

    public BankIdStatus getBankIdStatus() {
        if (message.toLowerCase().contains("avbruten")) {
            return BankIdStatus.CANCELLED;
        } else if (message.toLowerCase().contains("inget svar")) {
            return BankIdStatus.TIMEOUT;
        } else if (Objects.equal(status.toLowerCase(), "complete")) {
            return BankIdStatus.DONE;
        } else if (Objects.equal(status.toLowerCase(), "error")) {
            return BankIdStatus.FAILED_UNKNOWN;
        } else {
            return BankIdStatus.WAITING;
        }
    }
}
