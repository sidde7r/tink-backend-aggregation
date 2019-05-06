package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder;

import javax.annotation.Nonnull;
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

    IdModule build();
}
