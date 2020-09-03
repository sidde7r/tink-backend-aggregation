package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdPollRequest {

    @JsonProperty("order_ref")
    private String orderRef;

    public BankIdPollRequest(String orderRef) {
        this.orderRef = orderRef;
    }
}
