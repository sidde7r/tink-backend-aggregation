package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.GetPaymentLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
public class GetPaymentResponse {

    private CreatePaymentRequest paymentRequest;

    @JsonProperty("_links")
    private GetPaymentLinksEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(boolean paid) {
        return paymentRequest.toTinkPaymentResponse(paid);
    }
}
