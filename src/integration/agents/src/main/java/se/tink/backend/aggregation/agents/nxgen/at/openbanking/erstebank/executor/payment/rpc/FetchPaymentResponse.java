package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {

    private String transactionStatus;
    private String paymentId;
    private double transactionFees;
    private boolean transactionFeeIndicator;

    public FetchPaymentResponse() {}

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {
        Payment payment =
                new Payment.Builder()
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withDebtor(paymentRequest.getPayment().getDebtor())
                        .withAmount(paymentRequest.getPayment().getAmount())
                        .withExecutionDate(paymentRequest.getPayment().getExecutionDate())
                        .withCurrency(paymentRequest.getPayment().getAmount().getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                ErstebankConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.UNDEFINED)
                        .build();

        return new PaymentResponse(payment);
    }
}
