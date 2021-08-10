package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public final class CreatePaymentResponse {

    private final LinksEntity links;

    private final String paymentId;
    private final String transactionStatus;

    @JsonCreator
    public CreatePaymentResponse(
            @JsonProperty("_links") LinksEntity links,
            @JsonProperty("paymentId") String paymentId,
            @JsonProperty("transactionStatus") String transactionStatus) {
        this.links = links;
        this.paymentId = paymentId;
        this.transactionStatus = transactionStatus;
    }

    public PaymentResponse toTinkPayment(Payment payment) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(payment.getCreditor())
                        .withDebtor(payment.getDebtor())
                        .withExactCurrencyAmount(payment.getExactCurrencyAmount())
                        .withCurrency(payment.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(this.getPaymentStatus())
                        .withType(PaymentType.DOMESTIC);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    PaymentStatus getPaymentStatus() {
        return SkandiaConstants.PAYMENT_STATUS_MAPPER
                .translate(this.getTransactionStatus())
                .orElse(PaymentStatus.UNDEFINED);
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
