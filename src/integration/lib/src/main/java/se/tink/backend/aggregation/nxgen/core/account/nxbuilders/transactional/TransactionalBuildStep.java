package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface TransactionalBuildStep
        extends BuildStep<TransactionalAccount, TransactionalBuildStep> {}
