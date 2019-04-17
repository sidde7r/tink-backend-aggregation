package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher;

import java.util.stream.Stream;

public interface AccountStream {
    Stream<? extends IdentifiableAccount> stream();
}
