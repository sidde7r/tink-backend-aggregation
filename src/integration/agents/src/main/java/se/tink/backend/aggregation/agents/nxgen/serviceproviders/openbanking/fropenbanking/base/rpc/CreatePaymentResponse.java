package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    public PaymentResponse toTinkPaymentResponse(
            AccountEntity creditor,
            AccountEntity debtor,
            AmountEntity amount,
            PaymentType paymentType) {
        Payment tinkPayment =
                new Payment.Builder()
                        .withUniqueId(getPaymentId())
                        .withType(paymentType)
                        .withCurrency(amount.getCurrency())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .build();

        return new PaymentResponse(tinkPayment);
    }

    private String getPaymentId() {
        return links.getAuthorizationUrl().split("=")[1];
    }
}
