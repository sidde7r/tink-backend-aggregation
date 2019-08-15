package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String paymentId;
    private String transactionStatus;

    @JsonIgnore
    public PaymentResponse toTinkPayment(Creditor creditor, Debtor debtor, Amount amount) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withCurrency(amount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                FiduciaConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.SEPA);
        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
