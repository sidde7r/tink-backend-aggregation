package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public abstract class BerlinGroupBasePaymentResponse {

    private String transactionStatus;

    protected abstract PaymentResponse toTinkPaymentResponse(
            Payment payment, BerlinGroupPaymentType paymentType);

    Payment.Builder getBuildingPaymentResponse(
            Payment payment, BerlinGroupPaymentType paymentType) {
        return new Payment.Builder()
                .withType(paymentType.getTinkPaymentType())
                .withStatus(
                        BerlinGroupPaymentStatus.fromString(transactionStatus)
                                .getTinkPaymentStatus())
                .withCurrency(payment.getCurrency())
                .withAmount(
                        Amount.valueOf(
                                payment.getAmount().getCurrency(),
                                Double.valueOf(payment.getAmount().doubleValue() * 100).longValue(),
                                2))
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        payment.getCreditor().getAccountIdentifierType(),
                                        payment.getCreditor().getAccountNumber(),
                                        payment.getCreditor().getName())))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        payment.getDebtor().getAccountIdentifierType(),
                                        payment.getDebtor().getAccountNumber())))
                .withExecutionDate(null);
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
