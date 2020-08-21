package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice2ResponsePayload {

    @JsonProperty("trxid")
    private String transactionId;

    public String getTransactionId() {
        return transactionId;
    }
}
