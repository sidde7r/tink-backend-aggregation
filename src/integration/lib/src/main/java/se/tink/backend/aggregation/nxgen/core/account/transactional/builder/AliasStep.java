package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import javax.annotation.Nonnull;

public interface AliasStep<S> {

    /**
     * Sets the name of the account to the given value. Alias should be recognizable for the account
     * holder.
     *
     * @param alias The name/alias of the account.
     * @return The next step of the builder.
     */
    AccountIdentifierStep<S> setAlias(@Nonnull String alias);
}
