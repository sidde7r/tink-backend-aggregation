package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.PaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentResponse {

    @JsonProperty("paymentRequest")
    private PaymentEntity payment;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse() {

        InstructedAmountEntity amountEntity = payment.getAmountFromResponse();
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(amountEntity.getAmount(), amountEntity.getCurrency());

        return new PaymentResponse(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(
                                                payment.getBeneficiary()
                                                        .getCreditorAccount()
                                                        .getIban())))
                        .withDebtor(
                                new Debtor(
                                        new IbanIdentifier(payment.getDebtorAccount().getIban())))
                        .withUniqueId(payment.getResourceId())
                        .withExactCurrencyAmount(amount)
                        .withCurrency(amount.getCurrencyCode())
                        .withStatus(payment.getPaymentInformationStatus().mapToTinkPaymentStatus())
                        .build());
    }
}
