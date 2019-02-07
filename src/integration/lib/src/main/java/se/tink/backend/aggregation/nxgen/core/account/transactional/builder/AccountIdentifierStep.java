package se.tink.backend.aggregation.nxgen.core.account.transactional.builder;

import se.tink.libraries.account.AccountIdentifier;

import javax.annotation.Nonnull;

public interface AccountIdentifierStep<S> {

    /**
     * Adds an account identifier to the account. Account identifiers are ways of identifying the account, typically
     * used when doing transfer between accounts. e.g. IBAN, Account Number, etc.
     *
     * Account identifiers should be added here even if they have been added to the account in previous steps.
     *
     * This method can be invoked several times in order to add multiple identifiers.
     * @param identifier Identifier to be added.
     * @return The final step of the builder.
     */
    S addAccountIdentifier(@Nonnull AccountIdentifier identifier);
}
