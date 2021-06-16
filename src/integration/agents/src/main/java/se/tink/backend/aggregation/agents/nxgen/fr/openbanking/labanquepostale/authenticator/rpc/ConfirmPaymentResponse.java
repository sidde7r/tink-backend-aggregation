package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class ConfirmPaymentResponse {

    private PaymentEntity paymentRequest;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse() {

        AmountEntity amountEntity = paymentRequest.getAmountFromResponse();
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(amountEntity.getAmount(), amountEntity.getCurrency());

        return new PaymentResponse(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(
                                                paymentRequest
                                                        .getBeneficiary()
                                                        .getCreditorAccount()
                                                        .getIban())))
                        .withDebtor(
                                new Debtor(
                                        new IbanIdentifier(
                                                paymentRequest.getDebtorAccount().getIban())))
                        .withUniqueId(paymentRequest.getResourceId())
                        .withExactCurrencyAmount(amount)
                        .withCurrency(amount.getCurrencyCode())
                        .withStatus(paymentRequest.getPaymentStatus())
                        .build());
    }
}
