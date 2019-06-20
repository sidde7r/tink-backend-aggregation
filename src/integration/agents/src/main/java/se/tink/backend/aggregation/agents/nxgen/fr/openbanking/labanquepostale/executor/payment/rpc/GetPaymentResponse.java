package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.GetPaymentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
public class GetPaymentResponse {

    private CreatePaymentRequest paymentRequest;

    @JsonProperty("_links")
    private GetPaymentLinksEntity links;

    public PaymentResponse toTinkPaymentResponse() {
        return paymentRequest.toTinkPaymentResponse();
    }
}
