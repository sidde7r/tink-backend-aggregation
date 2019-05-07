package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder;

import javax.annotation.Nonnull;

public interface AccountNameStep<T> {
    /**
     * Sets the name of the account to the given value. Alias should be recognizable for the account
     * holder. If the supplied value is null, the alias will be set to the account number.
     *
     * @param name The name/alias of the account.
     * @return The next step of the builder.
     */
    IdentifierStep<T> withAccountName(@Nonnull String name);
}
