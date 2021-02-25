package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {
        Payment paymentFromRequest = paymentRequest.getPayment();
        Payment payment =
                new Payment.Builder()
                        .withCreditor(paymentFromRequest.getCreditor())
                        .withDebtor(paymentFromRequest.getDebtor())
                        .withExactCurrencyAmount(paymentFromRequest.getExactCurrencyAmount())
                        .withExecutionDate(paymentFromRequest.getExecutionDate())
                        .withCurrency(paymentFromRequest.getExactCurrencyAmount().getCurrencyCode())
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
