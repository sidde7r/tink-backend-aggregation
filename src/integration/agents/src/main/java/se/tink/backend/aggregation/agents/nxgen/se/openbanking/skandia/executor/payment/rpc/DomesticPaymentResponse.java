package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.TinkCreditorConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@Setter
@JsonObject
public class DomesticPaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String paymentId;
    private String transactionStatus;

    public PaymentResponse toTinkPayment(
            TinkCreditorConstructor creditor,
            AccountEntity debtor,
            ExactCurrencyAmount amount,
            PaymentStatus status) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withExactCurrencyAmount(amount)
                        .withCurrency(amount.getCurrencyCode())
                        .withUniqueId(paymentId)
                        .withStatus(status)
                        .withType(PaymentType.DOMESTIC);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
