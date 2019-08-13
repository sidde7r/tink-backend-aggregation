package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface TransactionalBuildStep
        extends BuildStep<TransactionalAccount, TransactionalBuildStep> {

    /**
     * Constructs an account from this builder.
     *
     * @return An account with the data provided to this builder.
     */
    Optional<TransactionalAccount> build();
}
