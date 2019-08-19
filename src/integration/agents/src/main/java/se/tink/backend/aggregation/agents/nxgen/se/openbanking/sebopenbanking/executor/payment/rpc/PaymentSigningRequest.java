package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentSigningRequest {
    private String authenticationMethodId;

    public PaymentSigningRequest() {}

    @JsonIgnore
    public PaymentSigningRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
