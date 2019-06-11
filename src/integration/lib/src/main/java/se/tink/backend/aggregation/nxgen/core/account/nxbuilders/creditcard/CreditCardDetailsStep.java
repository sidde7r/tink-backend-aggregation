package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;

public interface CreditCardDetailsStep<T> {

    WithIdStep<T> withCardDetails(CreditCardModule cardDetails);
}
