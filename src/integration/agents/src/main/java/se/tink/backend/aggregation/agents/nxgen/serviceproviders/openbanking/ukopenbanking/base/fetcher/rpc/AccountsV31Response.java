package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.AccountStream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsV31Response extends BaseV31Response<List<AccountEntity>>
        implements AccountStream {

    public Stream<AccountEntity> stream() {
        return getData().orElse(Collections.emptyList()).stream();
    }
}
