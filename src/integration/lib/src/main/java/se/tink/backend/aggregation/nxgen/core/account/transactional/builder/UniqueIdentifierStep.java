package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import javax.annotation.Nonnull;

public interface UniqueIdentifierStep<S> {

    /**
     * Sets the unique identifier of the account.
     *
     * An unique identifier should have the following attributes:
     *
     * 1. Must uniquely identify an account withing a credential.
     * 2. Is unlikely to change if the bank updates their API or database.
     *
     * Good unique identifiers are typically:
     *
     * - IBAN
     * - Account Number
     * - Masked Credit Card Number
     *
     * While bad unique identifiers are:
     *
     * - Name of the account
     * - The accounts API identifier
     *
     * @param uniqueIdentifier The unique identifier
     * @return The next step of the builder
     */
    AccountNumberStep<S> setUniqueIdentifier(@Nonnull String uniqueIdentifier);
}
