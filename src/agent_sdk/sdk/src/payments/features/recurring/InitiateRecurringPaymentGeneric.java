package se.tink.agent.sdk.payments.features.recurring;

import com.google.common.annotations.Beta;
import se.tink.agent.sdk.payments.recurring.GenericRecurringPaymentInitiator;

@Beta
public interface InitiateRecurringPaymentGeneric {
    GenericRecurringPaymentInitiator recurringPaymentInitiator();
}
