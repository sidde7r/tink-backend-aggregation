package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
public class GetPaymentResponse {
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorName;
    private String transactionStatus;

    public PaymentResponse toTinkPaymentResponse(Payment payment, DnbPaymentType dnbPaymentType) {
        Payment.Builder buildingPaymentResponse =
                new Builder()
                        .withUniqueId(payment.getUniqueId())
                        .withStatus(
                                DnbPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withType(dnbPaymentType.getTinkPaymentType())
                        .withCurrency(instructedAmount.getCurrency())
                        .withAmount(
                                Amount.valueOf(
                                        instructedAmount.getCurrency(),
                                        Double.valueOf(instructedAmount.getParsedAmount() * 100)
                                                .longValue(),
                                        2))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                payment.getCreditor().getAccountIdentifierType(),
                                                creditorAccount.getAccountNumber(),
                                                creditorName)))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                payment.getCreditor().getAccountIdentifierType(),
                                                debtorAccount.getAccountNumber())))
                        .withExecutionDate(null);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
