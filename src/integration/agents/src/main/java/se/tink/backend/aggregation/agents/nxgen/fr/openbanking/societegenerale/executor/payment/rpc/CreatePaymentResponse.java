package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.AppliedAuthenticationApproachEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentRequestLinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    private AppliedAuthenticationApproachEntity appliedAuthenticationApproach;

    @JsonProperty("_links")
    private PaymentRequestLinkEntity links;
    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentType type) {
        return new PaymentResponse(new Payment.Builder().build());
    }
}
