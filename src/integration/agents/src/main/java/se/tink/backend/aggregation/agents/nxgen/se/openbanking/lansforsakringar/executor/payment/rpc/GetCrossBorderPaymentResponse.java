package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.CrossBorderTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
public class GetCrossBorderPaymentResponse {
    private CrossBorderTransactionEntity crossBorderTransaction;

    public PaymentResponse toTinkPayment(String paymentId) {
        return crossBorderTransaction.toTinkPayment(paymentId);
    }
}
