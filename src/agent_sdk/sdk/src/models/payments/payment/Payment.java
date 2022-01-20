package se.tink.agent.sdk.models.payments.payment;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface Payment {

    String getTinkId();

    PaymentType getPaymentType();

    Optional<Debtor> tryGetDebtor();

    default Debtor getDebtor() {
        return this.tryGetDebtor()
                .orElseThrow(() -> new IllegalStateException("Debtor account was not set."));
    }

    Optional<String> getDebtorAccountMessage();

    Creditor getCreditor();

    ExactCurrencyAmount getAmount();

    RemittanceInformation getRemittanceInformation();

    LocalDate getExecutionDate();
}
