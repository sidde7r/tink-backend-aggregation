package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import javax.annotation.Nonnull;

public interface CardNumberStep<T> {

    CardBalanceStep<T> withCardNumber(@Nonnull String cardNumber);
}
