package se.tink.agent.sdk.models.payments.recurring_payment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import se.tink.agent.sdk.models.payments.payment.Creditor;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.PaymentType;
import se.tink.agent.sdk.models.payments.payment.RemittanceInformation;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface RecurringPayment {

    PaymentType getPaymentType();

    Debtor getDebtor();

    Creditor getCreditor();

    ExactCurrencyAmount getAmount();

    RemittanceInformation getRemittanceInformation();

    Frequency getFrequency();

    LocalDate getStartDate();

    LocalDate getEndDate();

    ExecutionRule getExecutionRule();

    // TODO: WHY boxed integer? Can it be null?
    Integer getDayOfMonth();

    DayOfWeek getDayOfWeek();
}
