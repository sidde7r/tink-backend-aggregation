package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollectBankIdRequest {
    private final String orderRef;

    public CollectBankIdRequest(String orderRef) {
        this.orderRef = orderRef;
    }
}
