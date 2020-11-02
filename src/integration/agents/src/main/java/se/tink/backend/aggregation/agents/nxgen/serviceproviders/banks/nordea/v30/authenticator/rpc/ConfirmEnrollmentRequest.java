package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmEnrollmentRequest {
    @JsonProperty("signing_order_reference")
    private String signingOrderReference;

    public ConfirmEnrollmentRequest(String signingOrderReference) {
        this.signingOrderReference = signingOrderReference;
    }
}
