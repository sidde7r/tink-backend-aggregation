package se.tink.agent.sdk.payments.features.single;

import com.google.common.annotations.Beta;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;

@Beta
public interface InitiateSinglePaymentGeneric {
    GenericSinglePaymentInitiator singlePaymentInitiator();
}
