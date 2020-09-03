package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmPaymentRequest {
    String psuAuthenticationFactor;
}
