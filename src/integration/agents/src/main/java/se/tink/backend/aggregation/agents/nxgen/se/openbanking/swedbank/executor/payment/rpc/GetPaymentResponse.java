package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.RemittanceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;
    private RemittanceInfoEntity remittanceInformationUnstructured;

    public PaymentResponse toTinkPaymentResponse(Payment payment, String transactionStatus) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(payment.getUniqueId())
                        .withType(payment.getType())
                        .withCurrency(instructedAmount.getCurrency())
                        .withExecutionDate(null)
                        .withStatus(
                                SwedbankPaymentStatus.mapToTinkPaymentStatus(
                                        SwedbankPaymentStatus.fromString(transactionStatus)))
                        .withAmount(
                                Amount.valueOf(
                                        instructedAmount.getCurrency(),
                                        Double.valueOf(instructedAmount.getParsedAmount() * 100)
                                                .longValue(),
                                        2))
                        .withCreditor(
                                creditorAccount.toTinkCreditor(
                                        payment.getCreditor().getAccountIdentifierType()))
                        .withDebtor(
                                debtorAccount.toTinkDebotor(
                                        payment.getDebtor().getAccountIdentifierType()));

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
