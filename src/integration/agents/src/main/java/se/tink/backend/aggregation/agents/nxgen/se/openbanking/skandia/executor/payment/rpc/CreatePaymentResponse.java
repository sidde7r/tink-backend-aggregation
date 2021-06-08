package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.TinkCreditorConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

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

    public PaymentResponse toTinkPayment(
            TinkCreditorConstructor creditor, AccountEntity debtor, ExactCurrencyAmount amount) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withExactCurrencyAmount(amount)
                        .withCurrency(amount.getCurrencyCode())
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
