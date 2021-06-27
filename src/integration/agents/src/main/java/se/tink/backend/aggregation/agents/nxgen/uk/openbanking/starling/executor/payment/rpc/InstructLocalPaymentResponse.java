package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity.ConsentInformation;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class InstructLocalPaymentResponse {

    private String paymentOrderUid;
    private ConsentInformation consentInformation;

    public String getPaymentOrderUid() {
        return paymentOrderUid;
    }

    public PaymentResponse toPaymentResponse() {
        Payment payment =
                new Payment.Builder()
                        .withUniqueId(paymentOrderUid)
                        .withStatus(PaymentStatus.CREATED)
                        .build();
        return new PaymentResponse(payment);
    }
}
