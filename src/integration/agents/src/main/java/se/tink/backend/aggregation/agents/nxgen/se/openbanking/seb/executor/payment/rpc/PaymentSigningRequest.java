package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentSigningRequest {
    private String authenticationMethodId;

    public PaymentSigningRequest() {}

    public PaymentSigningRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
