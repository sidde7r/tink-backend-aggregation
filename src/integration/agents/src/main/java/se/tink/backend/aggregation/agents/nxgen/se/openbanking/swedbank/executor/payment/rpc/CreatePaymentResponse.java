package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
public class CreatePaymentResponse {
    private String paymentId;
    private String transactionStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    public PaymentResponse toTinkPaymentResponse(
            Payment payment, SwedbankPaymentType swedbankPaymentType) {
        Payment.Builder buildingPaymentResponse =
                new Builder()
                        .withUniqueId(paymentId)
                        .withType(SwedbankPaymentType.mapToTinkPaymentType(swedbankPaymentType))
                        .withStatus(
                                SwedbankPaymentStatus.mapToTinkPaymentStatus(
                                        SwedbankPaymentStatus.fromString(transactionStatus)))
                        .withAmount(
                                Amount.valueOf(
                                        payment.getCurrency(),
                                        Double.valueOf(payment.getAmount().doubleValue() * 100)
                                                .longValue(),
                                        2))
                        .withCurrency(payment.getCurrency())
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                payment.getCreditor().getAccountIdentifierType(),
                                                payment.getCreditor().getAccountNumber())))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                payment.getDebtor().getAccountIdentifierType(),
                                                payment.getDebtor().getAccountNumber())))
                        .withExecutionDate(null);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
