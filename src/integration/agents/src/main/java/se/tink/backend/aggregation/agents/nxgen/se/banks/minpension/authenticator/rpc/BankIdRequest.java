package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdRequest {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("OrderRef")
    private String orderRef;

    @JsonProperty("ActivateAppOnSameDevice")
    private boolean activateAppOnSameDevice;

    public BankIdRequest() {}

    public BankIdRequest(String orderRef) {
        this.orderRef = orderRef;
    }
}
