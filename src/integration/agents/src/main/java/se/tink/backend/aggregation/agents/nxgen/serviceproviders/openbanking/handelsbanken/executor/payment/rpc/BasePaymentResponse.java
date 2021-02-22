package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class BasePaymentResponse {
    private String paymentId;
    private String transactionStatus;

    public PaymentResponse toTinkPaymentResponse(
            Payment payment, HandelsbankenPaymentType paymentType) {
        final Creditor creditor = payment.getCreditor();
        final Debtor debtor = payment.getDebtor();
        final ExactCurrencyAmount amount = payment.getExactCurrencyAmount();

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(getTinkCreditor(creditor))
                        .withDebtor(getTinkDebtor(debtor))
                        .withExactCurrencyAmount(amount)
                        .withCurrency(payment.getCurrency())
                        .withUniqueId(paymentId)
                        .withType(paymentType.getTinkPaymentType())
                        .withStatus(
                                HandelsbankenPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    private Creditor getTinkCreditor(Creditor creditor) {
        return new Creditor(
                AccountIdentifier.create(
                        creditor.getAccountIdentifierType(), creditor.getAccountNumber()));
    }

    private Debtor getTinkDebtor(Debtor debtor) {
        return new Debtor(
                AccountIdentifier.create(
                        debtor.getAccountIdentifierType(), debtor.getAccountNumber()));
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
