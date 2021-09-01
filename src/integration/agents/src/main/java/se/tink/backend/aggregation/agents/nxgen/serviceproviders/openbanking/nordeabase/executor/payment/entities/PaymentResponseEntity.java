package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@JsonObject
@Getter
public class PaymentResponseEntity {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("payment_status")
    private String paymentStatus;

    private double amount;
    private String currency;

    private CreditorEntity creditor;
    private DebtorEntity debtor;

    public String getPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public String toString() {
        return "Payment{"
                + "paymentStatus='"
                + paymentStatus
                + '\''
                + ", amount="
                + amount
                + ", currency='"
                + currency
                + '\''
                + ", creditor="
                + creditor
                + ", debtor="
                + debtor
                + '}';
    }

    public PaymentResponse toTinkPaymentResponse(
            PaymentType paymentType, PaymentServiceType paymentServiceType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withExactCurrencyAmount(
                                ExactCurrencyAmount.of(
                                        BigDecimal.valueOf(amount).setScale(2, RoundingMode.DOWN),
                                        currency))
                        .withExecutionDate(null)
                        .withCurrency(currency)
                        .withUniqueId(id)
                        .withStatus(
                                NordeaPaymentStatus.mapToTinkPaymentStatus(
                                        NordeaPaymentStatus.fromString(paymentStatus)))
                        .withType(paymentType)
                        .withPaymentServiceType(paymentServiceType);

        if (creditor.getReference() != null) {
            buildingPaymentResponse.withReference(creditor.getReference().toTinkReference());
        }

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public PaymentResponse toTinkPaymentResponse(PaymentType paymentType) {
        return toTinkPaymentResponse(paymentType, PaymentServiceType.SINGLE);
    }
}
