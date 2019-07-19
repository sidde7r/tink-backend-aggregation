package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String paymentId;
    private String transactionStatus;

    public PaymentResponse toTinkPaymentResponse(
            PaymentRequest paymentRequest, PaymentType paymentType) {
        Payment payment = paymentRequest.getPayment();

        Payment paymentResponse =
                new Payment.Builder()
                        .withCreditor(payment.getCreditor())
                        .withDebtor(payment.getDebtor())
                        .withAmount(payment.getAmount())
                        .withExecutionDate(payment.getExecutionDate())
                        .withCurrency(payment.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                SparebankPaymentStatus.mapToTinkPaymentStatus(
                                        SparebankPaymentStatus.fromString(transactionStatus)))
                        .withType(paymentType)
                        .build();

        return new PaymentResponse(paymentResponse);
    }

    public String getPaymentId() {
        return paymentId;
    }
}
