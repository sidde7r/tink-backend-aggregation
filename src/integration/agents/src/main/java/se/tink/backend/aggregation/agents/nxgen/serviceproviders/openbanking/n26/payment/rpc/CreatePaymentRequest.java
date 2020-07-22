package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    private Payload payload;

    public CreatePaymentRequest(String tokenId) {
        this.payload = new Payload(tokenId);
    }

    @AllArgsConstructor
    @JsonObject
    private static class Payload {

        private String tokenId;
    }
}
