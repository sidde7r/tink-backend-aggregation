package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;

public interface WithIdStep<T> {

    WithBalanceStep<T> withId(IdModule id);

}
