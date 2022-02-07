package se.tink.agent.runtime.models.payments;

import java.time.LocalDate;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.agent.sdk.models.payments.payment.Creditor;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment.PaymentType;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformation;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class PaymentImpl implements Payment {
    private final String tinkId;
    private final PaymentType type;
    @Nullable private final Debtor debtor;
    @Nullable private final String debtorAccountMessage;
    private final Creditor creditor;
    private final ExactCurrencyAmount amount;
    private final RemittanceInformation remittanceInformation;
    private final LocalDate executionDate;

    @Override
    public String getTinkId() {
        return this.tinkId;
    }

    @Override
    public PaymentType getType() {
        return this.type;
    }

    @Override
    public Optional<Debtor> tryGetDebtor() {
        return Optional.ofNullable(this.debtor);
    }

    @Override
    public Optional<String> getDebtorAccountMessage() {
        return Optional.ofNullable(this.debtorAccountMessage);
    }

    @Override
    public Creditor getCreditor() {
        return this.creditor;
    }

    @Override
    public ExactCurrencyAmount getAmount() {
        return this.amount;
    }

    @Override
    public RemittanceInformation getRemittanceInformation() {
        return this.remittanceInformation;
    }

    @Override
    public LocalDate getExecutionDate() {
        return this.executionDate;
    }
}
