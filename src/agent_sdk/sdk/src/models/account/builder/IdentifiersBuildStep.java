package se.tink.agent.sdk.models.account.builder;

import java.util.List;
import se.tink.libraries.account.AccountIdentifier;

public interface IdentifiersBuildStep<T> {
    T identifier(AccountIdentifier identifier);

    T identifiers(List<AccountIdentifier> identifiers);
}
