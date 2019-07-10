package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String transactionStatus;
    private String paymentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private String psuMessage;

    public PaymentResponse toTinkPaymentResponse(Payment payment, DnbPaymentType dnbPaymentType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withStatus(
                                DnbPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withType(dnbPaymentType.getTinkPaymentType())
                        .withCurrency(payment.getCurrency())
                        .withAmount(
                                Amount.valueOf(
                                        payment.getCurrency(),
                                        Double.valueOf(payment.getAmount().doubleValue() * 100)
                                                .longValue(),
                                        2))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                payment.getCreditor().getAccountIdentifierType(),
                                                payment.getCreditor().getAccountNumber(),
                                                payment.getCreditor().getName())))
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
