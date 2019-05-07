package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder;

import javax.annotation.Nonnull;

public interface UniqueIdStep<T> {

    /**
     * Sets the unique identifier of the account.
     *
     * <p>An unique identifier should have the following attributes:
     *
     * <ol>
     *   <li>Must uniquely identify an account withing a credential.
     *   <li>Is unlikely to change if the bank updates their API or database.
     * </ol>
     *
     * <p>Good unique identifiers are typically:
     *
     * <ul>
     *   <li>IBAN
     *   <li>Account Number
     *   <li>Masked Credit Card Number
     * </ul>
     *
     * <p>Bad unique identifiers are:
     *
     * <ul>
     *   <li>Name of the account
     *   <li>The account's API identifier
     * </ul>
     *
     * @param identifier The unique identifier
     * @return The next step of the builder
     */
    AccountNumberStep<T> withUniqueIdentifier(@Nonnull String identifier);
}
