package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard;

import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;

public interface CreditCardBuildStep extends BuildStep<CreditCardAccount, CreditCardBuildStep> {

    /**
     * Constructs an account from this builder.
     *
     * @return An account with the data provided to this builder.
     */
    CreditCardAccount build();
}
