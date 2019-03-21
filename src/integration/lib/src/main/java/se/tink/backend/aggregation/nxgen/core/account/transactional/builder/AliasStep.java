package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

public interface AliasStep<S> {

    /**
     * Sets the name of the account to the given value. Alias should be recognizable for the account
     * holder. If the supplied value is null, the alias will be set to the account number.
     *
     * @param alias The name/alias of the account.
     * @return The next step of the builder.
     */
    AccountIdentifierStep<S> setAlias(String alias);
}
