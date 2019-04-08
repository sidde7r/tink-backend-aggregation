package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;

public interface BuildStep<A extends Account, B extends BuildStep<A, B>> {

    /**
     * Stores a value meant to represent an identifier of an account in the banks API. Typically
     * this is used to retrieve transactions for or more information about the account in later
     * steps.
     *
     * @param identifier The id of the account in the context of the banks API.
     * @return The next step of the builder.
     */
    B setApiIdentifier(@Nonnull String identifier);

    /**
     * Sets the product name of the account. This is typically what the bank calls this type of
     * account but can be more specific than the account type. e.g. "Corporate Loan Account",
     * "Investment Holdings Depot".
     *
     * @param productName The name of the product.
     * @return The next step of the builder.
     */
    B setProductName(@Nonnull String productName);

    /**
     * Adds a name as a holder of the account. For shared accounts this method can be invoked
     * several times to add multiple holders.
     *
     * @param holderName Name of the account holder.
     * @return The next step of the builder.
     */
    B addHolderName(@Nonnull String holderName);

    /**
     * Adds an account identifier to the account. Account identifiers are ways of identifying the
     * account, typically used when doing transfer between accounts. e.g. IBAN, Account Number, etc.
     *
     * <p>Account identifiers should be added here even if they have been added to the account in
     * previous steps.
     *
     * <p>This method can be invoked several times in order to add multiple identifiers.
     *
     * @param identifier Identifier to be added.
     * @return The final step of the builder.
     */
    B addAccountIdentifier(@Nonnull AccountIdentifier identifier);

    B addAccountFlags(@Nonnull AccountFlag... accountFlags);

    /**
     * Stores the value under the given key in temporary storage.
     *
     * @param key Key to store the value under.
     * @param value The value to be stored.
     * @return The final step of the builder.
     */
    <V> B putInTemporaryStorage(@Nonnull String key, @Nonnull V value);

    /**
     * Constructs an account from this builder.
     *
     * @return An account with the data provided to this builder.
     */
    A build();
}
