package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmPaymentResponse extends NordeaBaseResponse {

    private PaymentResponseEntity response;

    public PaymentResponseEntity getPaymentResponse() {
        return response;
    }
}
