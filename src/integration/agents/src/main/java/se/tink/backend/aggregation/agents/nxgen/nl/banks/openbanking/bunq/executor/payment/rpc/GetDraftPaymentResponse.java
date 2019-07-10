package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities.PaymentResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetDraftPaymentResponse {

    @JsonProperty("DraftPayment")
    private PaymentResponseEntity draftPayment;

    public PaymentResponseEntity getDraftPayment() {
        return draftPayment;
    }
}
