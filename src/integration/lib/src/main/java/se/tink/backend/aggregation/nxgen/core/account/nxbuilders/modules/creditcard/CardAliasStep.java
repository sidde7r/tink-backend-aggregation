package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import javax.annotation.Nonnull;

public interface CardAliasStep<T> {

    T withCardAlias(@Nonnull String cardAlias);
}
