package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.util.AccountTypePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
public class CreatePaymentResponse {
    private String paymentId;
    private String transactionStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            AccountEntity creditor,
            AccountEntity debtor,
            AmountEntity amount,
            SwedbankPaymentType swedbankPaymentType,
            AccountTypePair accountTypePair) {
        Payment.Builder buildingPaymentResponse =
                new Builder()
                        .withUniqueId(paymentId)
                        .withType(swedbankPaymentType.getTinkPaymentType())
                        .withStatus(
                                SwedbankPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCurrency(amount.getCurrency())
                        .withCreditor(
                                creditor.toTinkCreditor(accountTypePair.getCreditorAccountType()))
                        .withDebtor(debtor.toTinkDebtor(accountTypePair.getDebtorAccountType()));

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
