package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String transactionStatus;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("_links")
    private Href links;

    private String psuMessage;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            AccountEntity creditor,
            AccountEntity debtor,
            AmountEntity amount,
            DnbPaymentType dnbPaymentType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withStatus(
                                DnbPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withType(dnbPaymentType.getTinkPaymentType())
                        .withCurrency(amount.getCurrency())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    @JsonIgnore
    public Href getLinks() {
        return links;
    }

    @JsonIgnore
    public String getPaymentId() {
        return paymentId;
    }
}
