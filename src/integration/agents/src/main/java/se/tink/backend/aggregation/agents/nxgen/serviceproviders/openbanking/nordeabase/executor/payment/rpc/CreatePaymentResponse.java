package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.PaymentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@JsonObject
public class CreatePaymentResponse extends NordeaBaseResponse {

    private PaymentResponseEntity response;

    public PaymentResponseEntity getResponse() {
        return response;
    }

    public PaymentResponse toTinkPaymentResponse(
            PaymentType paymentType, PaymentServiceType paymentServiceType) {
        return response.toTinkPaymentResponse(paymentType, paymentServiceType);
    }
}
