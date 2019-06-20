package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.AppliedAuthenticationApproachEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private AppliedAuthenticationApproachEntity appliedAuthenticationApproach;

    @JsonProperty("_links")
    private LinkEntity links;

    public PaymentResponse toTinkPaymentResponse(PaymentType sepa) {

        return new PaymentResponse(new Payment.Builder().build());
    }
}
