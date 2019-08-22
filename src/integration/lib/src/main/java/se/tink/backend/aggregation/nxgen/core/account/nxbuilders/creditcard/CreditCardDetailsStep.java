package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard;

import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.WithFlagPolicyStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;

public interface CreditCardDetailsStep<T> {

    WithFlagPolicyStep<WithIdStep<T>, AccountTypeMapper> withCardDetails(
            CreditCardModule cardDetails);
}
