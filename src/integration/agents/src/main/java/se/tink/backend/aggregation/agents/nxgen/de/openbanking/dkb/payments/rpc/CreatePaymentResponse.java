package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.enums.DkbPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
public class CreatePaymentResponse {
    private String transactionStatus;
    private String paymentId;

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        Payment.Builder buildingPaymentResponse =
                new Builder()
                        .withUniqueId(paymentId)
                        .withCreditor(payment.getCreditor())
                        .withDebtor(payment.getDebtor())
                        .withAmount(payment.getAmount())
                        .withStatus(
                                DkbPaymentStatus.mapToTinkPaymentStatus(
                                        DkbPaymentStatus.fromString(transactionStatus)));

        Payment tinkPayment = buildingPaymentResponse.build();
        return new PaymentResponse(tinkPayment);
    }
}
