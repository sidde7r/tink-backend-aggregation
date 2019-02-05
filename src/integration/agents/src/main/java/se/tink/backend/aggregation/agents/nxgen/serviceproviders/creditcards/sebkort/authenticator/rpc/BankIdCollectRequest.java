package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCollectRequest {
    private String orderRef;

    public BankIdCollectRequest(String orderRef) {
        this.orderRef = orderRef;
    }
}
