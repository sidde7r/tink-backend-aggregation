package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public interface WithTypeStep<T> {

    T withType(@Nonnull TransactionalAccountType accountType);
}
