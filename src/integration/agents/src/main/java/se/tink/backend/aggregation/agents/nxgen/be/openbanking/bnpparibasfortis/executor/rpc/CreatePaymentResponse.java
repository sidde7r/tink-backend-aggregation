package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums.BnpParibasFortisPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String appliedAuthenticationApproach;

    public PaymentResponse toTinkPaymentResponse(
            AccountEntity creditor,
            AccountEntity debtor,
            AmountEntity amount,
            BnpParibasFortisPaymentType paymentType) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(getPaymentId())
                        .withType(paymentType.getTinkPaymentType())
                        .withCurrency(amount.getCurrency())
                        .withAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    private String getPaymentId() {
        return links.getAuthorizationUrl().split("/")[4];
    }
}
