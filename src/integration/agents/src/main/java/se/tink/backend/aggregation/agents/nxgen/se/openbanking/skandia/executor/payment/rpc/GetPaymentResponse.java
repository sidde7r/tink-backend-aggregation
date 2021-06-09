package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public final class GetPaymentResponse {
    private final String transactionStatus;

    @JsonCreator
    public GetPaymentResponse(@JsonProperty("transactionStatus") String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public PaymentResponse toTinkPayment(Payment payment) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(payment.getCreditor())
                        .withDebtor(payment.getDebtor())
                        .withExactCurrencyAmount(payment.getExactCurrencyAmount())
                        .withCurrency(payment.getCurrency())
                        .withUniqueId(payment.getUniqueId())
                        .withStatus(
                                SkandiaConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(payment.getType());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
