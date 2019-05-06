package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;

public interface WithTypeStep<T> {

    WithIdStep<T> withType(AccountTypes accountType);

}
