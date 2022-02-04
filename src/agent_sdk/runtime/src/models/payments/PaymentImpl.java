package se.tink.agent.runtime.models.payments;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.agent.sdk.models.payments.payment.Creditor;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment.PaymentType;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformation;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class PaymentImpl implements Payment {
    @Override
    public String getTinkId() {
        return null;
    }

    @Override
    public PaymentType getPaymentType() {
        return null;
    }

    @Override
    public Optional<Debtor> tryGetDebtor() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getDebtorAccountMessage() {
        return Optional.empty();
    }

    @Override
    public Creditor getCreditor() {
        return null;
    }

    @Override
    public ExactCurrencyAmount getAmount() {
        return null;
    }

    @Override
    public RemittanceInformation getRemittanceInformation() {
        return null;
    }

    @Override
    public LocalDate getExecutionDate() {
        return null;
    }
}
