package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public interface WithTypeStep<T> {

    WithIdStep<T> withType(TransactionalAccountType accountType);
}
