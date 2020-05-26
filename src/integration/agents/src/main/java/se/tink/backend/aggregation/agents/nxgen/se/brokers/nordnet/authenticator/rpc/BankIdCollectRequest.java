package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCollectRequest {

    private String orderRef;

    public BankIdCollectRequest(String orderRef) {
        this.orderRef = orderRef;
    }

    public String getOrderRef() {
        return orderRef;
    }
}
