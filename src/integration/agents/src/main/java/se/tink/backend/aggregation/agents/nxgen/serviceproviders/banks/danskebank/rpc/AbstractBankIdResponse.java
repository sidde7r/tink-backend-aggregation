package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractBankIdResponse extends AbstractResponse {
    @JsonProperty("AutostartToken")
    private String autostartToken;

    @JsonProperty("OrderRef")
    private String orderReference;

    @JsonProperty("Status")
    private String status;

    public String getAutostartToken() {
        return autostartToken;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public String getStatus() {
        return status;
    }
}
