package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.RemittanceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
public class GetPaymentResponse {
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;
    private RemittanceInfoEntity remittanceInfoEntity;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(Payment payment, String transactionStatus) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(payment.getUniqueId())
                        .withType(payment.getType())
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(
                                SwedbankPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withAmount(instructedAmount.toTinkAmount())
                        .withCreditor(
                                creditorAccount.toTinkCreditor(
                                        payment.getCreditor().getAccountIdentifierType()))
                        .withDebtor(
                                debtorAccount.toTinkDebtor(
                                        payment.getDebtor().getAccountIdentifierType()));

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
