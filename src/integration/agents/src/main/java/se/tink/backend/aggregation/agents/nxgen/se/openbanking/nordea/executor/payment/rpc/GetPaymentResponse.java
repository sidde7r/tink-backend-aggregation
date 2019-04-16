package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;

@JsonObject
public class GetPaymentResponse extends NordeaBaseResponse {

    private PaymentResponseEntity response;

    public PaymentResponseEntity getResponse() {
        return response;
    }

    public PaymentResponse toTinkPaymentResponse(PaymentType paymentType) {
        return response.toTinkPaymentResponse(paymentType);
    }
}
