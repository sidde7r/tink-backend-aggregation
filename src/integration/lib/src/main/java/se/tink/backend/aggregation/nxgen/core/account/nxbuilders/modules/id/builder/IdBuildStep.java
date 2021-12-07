package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;

public interface IdBuildStep {

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
    IdBuildStep addIdentifier(@Nonnull AccountIdentifier identifier);

    /**
     * Sets the product name of the account. This is typically what the bank calls this type of
     * account but can be more specific than the account type. e.g. "Corporate Loan Account",
     * "Investment Holdings Depot".
     *
     * @param productName The name of the product.
     * @return The next step of the builder.
     */
    IdBuildStep setProductName(@Nullable String productName);

    /**
     * Sets the reference of the account in the bank's API, i.e. a resource identifier. This value
     * will be used to access resources attached to this account, e.g. transactions.
     *
     * @param reference The account reference.
     * @return The next step of the builder.
     */
    IdBuildStep setBankApiReference(String reference);

    /**
     * Sets the reference of the account in the bank's API, i.e. a resource identifier. This value
     * will be used to access resources attached to this account, e.g. transactions.
     *
     * <p>Use this method when the API require more than one value to reference the account, i.e. a
     * data structure with more than one field. The object must be serializable using Jackson.
     *
     * @param reference The account reference. Must be serializable using Jackson.
     * @return The next step of the builder.
     */
    IdBuildStep setBankApiReference(Object reference);

    IdModule build();
}
