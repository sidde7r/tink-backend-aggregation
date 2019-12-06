package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
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
        Amount amount = payment.getAmount();
        Creditor creditor = payment.getCreditor();
        Debtor debtor = payment.getDebtor();

        return new Payment.Builder()
                .withType(paymentType.getTinkPaymentType())
                .withStatus(
                        BerlinGroupPaymentStatus.fromString(transactionStatus)
                                .getTinkPaymentStatus())
                .withCurrency(payment.getCurrency())
                .withAmount(new Amount(amount.getCurrency(), amount.getValue()))
                .withCreditor(new Creditor(new IbanIdentifier(creditor.getAccountNumber())))
                .withDebtor(new Debtor(new IbanIdentifier(debtor.getAccountNumber())));
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
