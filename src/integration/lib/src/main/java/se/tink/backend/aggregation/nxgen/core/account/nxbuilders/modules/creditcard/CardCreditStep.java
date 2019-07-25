package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import javax.annotation.Nonnull;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface CardCreditStep<T> {

    CardAliasStep<T> withAvailableCredit(@Nonnull ExactCurrencyAmount availableCredit);
}
