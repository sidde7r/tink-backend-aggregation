package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.enums.BnpParibasPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    public PaymentResponse toTinkPaymentResponse(
            AccountEntity creditor,
            AccountEntity debtor,
            AmountEntity amount,
            BnpParibasPaymentType paymentType) {
        Payment tinkPayment =
                new Payment.Builder()
                        .withUniqueId(getPaymentId())
                        .withType(paymentType.getPaymentType())
                        .withCurrency(amount.getCurrency())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .build();

        return new PaymentResponse(tinkPayment);
    }

    private String getPaymentId() {
        return links.getAuthorizationUrl().split("/?i=")[1];
    }

    public LinksEntity getLinks() {
        return links;
    }
}
