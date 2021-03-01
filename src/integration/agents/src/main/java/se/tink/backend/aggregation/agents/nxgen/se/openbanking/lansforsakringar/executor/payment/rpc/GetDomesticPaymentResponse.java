package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.DomesticTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
public class GetDomesticPaymentResponse {
    private DomesticTransactionEntity domesticPayment;

    public PaymentResponse toTinkPayment(String paymentId) {
        return domesticPayment.toTinkPayment(paymentId);
    }
}
