package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice3ResponsePayload {

    @JsonProperty private String seed;

    @JsonProperty("trxid")
    private String transactionId;

    public String getSeed() {
        return seed;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
